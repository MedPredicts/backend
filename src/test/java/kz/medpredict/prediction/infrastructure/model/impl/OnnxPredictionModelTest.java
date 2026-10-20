package kz.medpredict.prediction.infrastructure.model.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.HashMap;
import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.prediction.infrastructure.model.ModelArtifactMetadataTest;
import kz.medpredict.prediction.validation.InputValidator;
import kz.medpredict.prediction.validation.ModelMetadataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnnxPredictionModelTest {
    private static final Map<String, Object> VALID = Map.of(
            "age", 52, "sex", "male", "bmi", 27.4, "totalCholesterol", 5.6, "glucose", 5.9);
    @TempDir Path directory;
    private final ObjectMapper mapper = new ObjectMapper();
    private final jakarta.validation.ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @AfterEach
    void closeFactory() {
        factory.close();
    }

    @Test
    void missingBundleNeverFallsBackToDemo() {
        try (var model = createModel()) {
            assertThat(model.status().isReady()).isFalse();
            assertThat(model.status().getStatus()).isEqualTo("UNAVAILABLE");
            assertThat(model.status().getModelVersion()).isNull();
            assertThatThrownBy(model::info).isInstanceOf(ModelUnavailableException.class);
            assertThatThrownBy(() -> model.predict(Map.of())).isInstanceOf(ModelUnavailableException.class);
        }
    }

    @Test
    void checksumMismatchAndMalformedMetadataAreUnavailableWithoutDetails() throws Exception {
        Files.write(directory.resolve("model.onnx"), new byte[]{1, 2, 3});
        for (String metadata : new String[]{ModelArtifactMetadataTest.METADATA, "{}", "null", "{"}) {
            Files.writeString(directory.resolve("metadata.json"), metadata);
            try (var model = createModel()) {
                assertThat(model.status().getStatus()).isEqualTo("INVALID_ARTIFACT");
                assertThat(model.status().isReady()).isFalse();
                assertThat(mapper.writeValueAsString(model.status())).doesNotContain(directory.toString(), "Exception", "sha256");
                assertThatThrownBy(model::info).isInstanceOf(ModelUnavailableException.class);
            }
        }
    }

    @Test
    void corruptOnnxWithMatchingChecksumIsStillUnavailable() throws Exception {
        byte[] bytes = {1, 2, 3};
        Files.write(directory.resolve("model.onnx"), bytes);
        var metadata = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(ModelArtifactMetadataTest.METADATA);
        metadata.put("sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        Files.write(directory.resolve("metadata.json"), mapper.writeValueAsBytes(metadata));
        try (var model = createModel()) {
            assertThat(model.status().getStatus()).isEqualTo("INVALID_ARTIFACT");
            assertThatThrownBy(() -> model.predict(Map.of())).isInstanceOf(ModelUnavailableException.class);
        }
    }

    @Test
    void executesNativeGraphWithExactFeatureOrderAndSexEncoding() throws Exception {
        fixture("synthetic-affine.onnx");
        try (var model = createModel()) {
            assertThat(model.status().isReady()).isTrue();
            assertThat(model.status().getStatus()).isEqualTo("READY");
            var male = model.predict(VALID);
            assertThat(male.getPredictedValue()).isCloseTo(286.1, org.assertj.core.data.Offset.offset(0.0001));
            assertThat(male.getModelVersion()).isEqualTo("synthetic-test-v1");
            assertThat(male.getDemo()).isFalse();
            var female = new HashMap<>(VALID);
            female.put("sex", "female");
            assertThat(model.predict(female).getPredictedValue() - male.getPredictedValue()).isEqualTo(2.0);
            female.put("age", 17);
            assertThatThrownBy(() -> model.predict(female)).isInstanceOf(InputValidationException.class);
        }
    }

    @Test
    void rejectsWrongInputAndOutputTensorShapesAtStartup() throws Exception {
        for (String name : new String[]{"wrong-input-shape.onnx", "wrong-output-shape.onnx"}) {
            fixture(name);
            try (var model = createModel()) {
                assertThat(model.status().isReady()).isFalse();
                assertThat(model.status().getStatus()).isEqualTo("INVALID_ARTIFACT");
                assertThatThrownBy(() -> model.predict(VALID)).isInstanceOf(ModelUnavailableException.class);
            }
        }
    }

    @Test
    void nonFiniteNativeInferenceNeverReturnsASuccessfulPrediction() throws Exception {
        fixture("non-finite-output.onnx");
        try (var model = createModel()) {
            assertThat(model.status().isReady()).isTrue();
            assertThatThrownBy(() -> model.predict(VALID)).isInstanceOf(ModelUnavailableException.class);
        }
    }

    private void fixture(String name) throws Exception {
        byte[] bytes;
        try (var resource = getClass().getResourceAsStream("/onnx/" + name)) {
            bytes = resource.readAllBytes();
        }
        Files.write(directory.resolve("model.onnx"), bytes);
        var metadata = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(ModelArtifactMetadataTest.METADATA);
        metadata.put("sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        Files.write(directory.resolve("metadata.json"), mapper.writeValueAsBytes(metadata));
    }

    private OnnxPredictionModel createModel() {
        return new OnnxPredictionModel(mapper, new ModelMetadataValidator(factory.getValidator()), new InputValidator(), directory.toString(), "");
    }
}
