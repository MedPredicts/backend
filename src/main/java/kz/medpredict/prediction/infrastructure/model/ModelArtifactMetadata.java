package kz.medpredict.prediction.infrastructure.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kz.medpredict.prediction.model.dto.PredictionFieldOptionResponse;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.validation.ModelMetadataValidator;

/** Metadata is the input domain of the trained artifact, not a set of clinical normal ranges. */
public record ModelArtifactMetadata(PredictionModelInfoResponse info, String sha256, String disclaimer) {
    public static final List<String> FEATURE_ORDER = List.of("age", "sex", "bmi", "totalCholesterol", "glucose");
    public static final String PREDICTION_TYPE = "estimated_current_measurement";
    private static final Map<String, String> UNITS = Map.of(
            "age", "лет", "bmi", "кг/м²", "totalCholesterol", "ммоль/л", "glucose", "ммоль/л");

    public static ModelArtifactMetadata read(JsonNode root, ModelMetadataValidator validator, String expectedVersion) {
        require(root != null && root.isObject() && root.path("schemaVersion").isInt() && root.path("schemaVersion").intValue() == 1);
        String version = text(root, "modelVersion");
        require(version.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}"));
        require(expectedVersion.isBlank() || expectedVersion.equals(version));
        String hash = text(root, "sha256");
        require(hash.matches("[a-f0-9]{64}"));
        require("features".equals(text(root, "inputName")) && "predictedValue".equals(text(root, "outputName")));
        require(PREDICTION_TYPE.equals(text(root, "predictionType")));
        require(root.path("demo").isBoolean() && !root.path("demo").booleanValue());
        require(root.path("sexEncoding").isObject() && root.path("sexEncoding").size() == 2
                && root.path("sexEncoding").path("male").isInt() && root.path("sexEncoding").path("male").intValue() == 0
                && root.path("sexEncoding").path("female").isInt() && root.path("sexEncoding").path("female").intValue() == 1);
        var order = root.path("featureOrder");
        require(order.isArray() && order.size() == FEATURE_ORDER.size());
        var fields = root.path("fields");
        require(fields.isArray() && fields.size() == FEATURE_ORDER.size());
        List<PredictionFieldResponse> definitions = new ArrayList<>();
        for (int i = 0; i < FEATURE_ORDER.size(); i++) {
            String key = FEATURE_ORDER.get(i);
            require(order.get(i).isTextual() && key.equals(order.get(i).textValue()));
            var field = fields.get(i);
            require(key.equals(text(field, "key")) && field.path("required").isBoolean() && field.path("required").booleanValue());
            String label = text(field, "label");
            if ("sex".equals(key)) {
                require("select".equals(text(field, "type")));
                var options = field.path("options");
                require(options.isArray() && options.size() == 2);
                List<PredictionFieldOptionResponse> categories = new ArrayList<>();
                for (var option : options) {
                    categories.add(new PredictionFieldOptionResponse(text(option, "value"), text(option, "label")));
                }
                require(categories.stream().map(PredictionFieldOptionResponse::getValue).toList().equals(List.of("male", "female")));
                definitions.add(new PredictionFieldResponse(key, label, "select", true, null, null, null, null, categories));
            } else {
                require("number".equals(text(field, "type")) && UNITS.get(key).equals(text(field, "unit")));
                double min = finiteNumber(field, "min");
                double max = finiteNumber(field, "max");
                require(min <= max && min >= -Float.MAX_VALUE && max <= Float.MAX_VALUE);
                Boolean integer = field.hasNonNull("integer") ? booleanValue(field, "integer") : null;
                require(!"age".equals(key) || Boolean.TRUE.equals(integer));
                definitions.add(new PredictionFieldResponse(key, label, "number", true, UNITS.get(key), min, max, integer, null));
            }
        }
        require("мм рт. ст.".equals(text(root, "unit")));
        require(root.path("decimalPlaces").isInt());
        var info = new PredictionModelInfoResponse(text(root, "indicatorName"), text(root, "unit"),
                root.path("decimalPlaces").intValue(), version, definitions, PREDICTION_TYPE, false);
        validator.validate(info);
        return new ModelArtifactMetadata(info, hash, text(root, "disclaimer"));
    }

    private static String text(JsonNode node, String key) {
        require(node.path(key).isTextual() && !node.path(key).textValue().isBlank());
        return node.path(key).textValue();
    }

    private static double finiteNumber(JsonNode node, String key) {
        require(node.path(key).isNumber() && Double.isFinite(node.path(key).doubleValue()));
        return node.path(key).doubleValue();
    }

    private static boolean booleanValue(JsonNode node, String key) {
        require(node.path(key).isBoolean());
        return node.path(key).booleanValue();
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Incompatible model artifact metadata");
        }
    }
}
