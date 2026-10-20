package kz.medpredict.prediction.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.prediction.validation.ValidPredictionInput;
import lombok.EqualsAndHashCode;

/** A flat JSON object whose fields are defined by the active model metadata. */
@ValidPredictionInput
@EqualsAndHashCode
@Schema(description = "Flat model-specific input, without an envelope; metadata determines valid fields.")
public final class PredictionCreateRequest {
    private final JsonNode values;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public PredictionCreateRequest(JsonNode values) {
        if (values == null || !values.isObject()) {
            throw new InvalidRequestException();
        }
        this.values = values.deepCopy();
    }

    @Schema(hidden = true)
    @JsonValue
    public JsonNode getValues() {
        return values.deepCopy();
    }
}
