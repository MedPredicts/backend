package kz.medpredict.prediction.validation;

import jakarta.validation.Validation;
import java.util.List;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.prediction.model.dto.PredictionFieldOptionResponse;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelMetadataValidatorTest {
    @Test
    void rejectsInconsistentSchemasBeforeInference() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = new ModelMetadataValidator(factory.getValidator());
            var field = number("value", 0.0, 10.0);
            for (var fields : List.of(
                    List.of(field, field), List.of(number("constructor", null, null)),
                    List.of(number("bad-key", null, null)), List.of(number("value", 10.0, 0.0)),
                    List.of(number("value", Double.NaN, null)),
                    List.of(new PredictionFieldResponse("category", "Category", "select", true,
                            null, null, null, null, List.of())),
                    List.of(new PredictionFieldResponse("category", "Category", "select", true,
                            null, null, null, null, List.of(new PredictionFieldOptionResponse("a", "A"),
                                    new PredictionFieldOptionResponse("a", "Duplicate")))))) {
                var metadata = new PredictionModelInfoResponse("Target", "units", 1, "v1", fields);
                assertThatThrownBy(() -> validator.validate(metadata)).isInstanceOf(InvalidModelResponseException.class);
            }
        }
    }

    private PredictionFieldResponse number(String key, Double min, Double max) {
        return new PredictionFieldResponse(key, "Value", "number", true, "units", min, max, null, null);
    }
}
