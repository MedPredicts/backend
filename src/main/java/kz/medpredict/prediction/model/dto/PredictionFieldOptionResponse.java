package kz.medpredict.prediction.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Schema(description = "A permitted category value and its display label.")
public final class PredictionFieldOptionResponse {
    @NotBlank
    @Schema(description = "Category string sent in the request.")
    private final String value;
    @NotBlank
    @Schema(description = "Provider-supplied display label.")
    private final String label;

}
