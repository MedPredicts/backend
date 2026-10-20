package kz.medpredict.prediction.infrastructure.model.impl;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxJavaType;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.core.exception.ServiceValidationException;
import kz.medpredict.prediction.infrastructure.model.ModelArtifactMetadata;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionModelStatusResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.validation.InputValidator;
import kz.medpredict.prediction.validation.ModelMetadataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("onnx & !demo")
public final class OnnxPredictionModel implements PredictionModel, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OnnxPredictionModel.class);
    private final OrtSession session;
    private final ModelArtifactMetadata metadata;
    private final PredictionModelStatusResponse status;
    private final ObjectMapper objectMapper;
    private final InputValidator inputValidator;

    public OnnxPredictionModel(ObjectMapper objectMapper, ModelMetadataValidator metadataValidator, InputValidator inputValidator,
            @Value("${medpredict.model.directory}") String directory,
            @Value("${medpredict.model.expected-version:}") String expectedVersion) {
        this.objectMapper = objectMapper;
        this.inputValidator = inputValidator;
        OrtSession loaded = null;
        ModelArtifactMetadata artifact = null;
        String state = "UNAVAILABLE";
        try {
            var bundle = Path.of(directory);
            var modelPath = bundle.resolve("model.onnx");
            var metadataPath = bundle.resolve("metadata.json");
            if (Files.isRegularFile(modelPath) && Files.isRegularFile(metadataPath)) {
                state = "INVALID_ARTIFACT";
                artifact = ModelArtifactMetadata.read(objectMapper.readTree(Files.readAllBytes(metadataPath)), metadataValidator, expectedVersion);
                byte[] bytes = Files.readAllBytes(modelPath);
                if (!artifact.sha256().equals(sha256(bytes))) {
                    throw new IllegalArgumentException("Model checksum mismatch");
                }
                var environment = OrtEnvironment.getEnvironment();
                environment.setTelemetry(false);
                try (var options = new OrtSession.SessionOptions()) {
                    options.setIntraOpNumThreads(1);
                    loaded = environment.createSession(bytes, options);
                }
                validateTensor(loaded.getInputInfo(), "features", 5);
                validateTensor(loaded.getOutputInfo(), "predictedValue", 1);
                state = "READY";
            }
        } catch (IOException | OrtException | IllegalArgumentException | ServiceValidationException | LinkageError exception) {
            closeSession(loaded);
            loaded = null;
            artifact = null;
            LOG.warn("Trained model is unavailable: invalid artifact or unsupported ONNX runtime");
        }
        this.session = loaded;
        this.metadata = artifact;
        this.status = new PredictionModelStatusResponse(loaded != null, state,
                loaded == null ? null : artifact.info().getModelVersion(), false);
    }

    @Override
    public PredictionModelInfoResponse info() {
        requireReady();
        return metadata.info();
    }

    @Override
    public PredictionModelStatusResponse status() {
        return status;
    }

    @Override
    public PredictionResultResponse predict(Map<String, Object> input) {
        requireReady();
        var validated = inputValidator.validate(objectMapper.valueToTree(input), metadata.info());
        float[][] features = new float[1][5];
        for (int index = 0; index < ModelArtifactMetadata.FEATURE_ORDER.size(); index++) {
            String key = ModelArtifactMetadata.FEATURE_ORDER.get(index);
            features[0][index] = "sex".equals(key) ? ("male".equals(validated.get(key)) ? 0 : 1)
                    : ((Number) validated.get(key)).floatValue();
        }
        try (var tensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), features);
                var result = session.run(Map.of("features", tensor))) {
            Object value = result.get("predictedValue").orElseThrow(ModelUnavailableException::new).getValue();
            if (!(value instanceof float[][] values) || values.length != 1 || values[0].length != 1
                    || !Float.isFinite(values[0][0])) {
                throw new ModelUnavailableException();
            }
            return new PredictionResultResponse(values[0][0], metadata.info().getUnit(), metadata.info().getModelVersion(),
                    metadata.disclaimer(), ModelArtifactMetadata.PREDICTION_TYPE, false);
        } catch (OrtException exception) {
            throw new ModelUnavailableException();
        }
    }

    private void requireReady() {
        if (session == null) {
            throw new ModelUnavailableException();
        }
    }

    private static void validateTensor(Map<String, NodeInfo> nodes, String name, int width) {
        if (nodes.size() != 1 || !nodes.containsKey(name) || !(nodes.get(name).getInfo() instanceof TensorInfo tensor)
                || tensor.type != OnnxJavaType.FLOAT || tensor.getShape().length != 2
                || (tensor.getShape()[0] != -1 && tensor.getShape()[0] != 1) || tensor.getShape()[1] != width) {
            throw new IllegalArgumentException("Incompatible ONNX tensor schema");
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the JVM", exception);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        closeSession(session);
    }

    private static void closeSession(OrtSession value) {
        if (value != null) {
            try {
                value.close();
            } catch (OrtException exception) {
                LOG.warn("ONNX session could not be closed");
            }
        }
    }
}
