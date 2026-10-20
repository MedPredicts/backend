package kz.medpredict.prediction.model.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class PredictionResultResponse {
    private final double predictedValue;
    @NotBlank
    private final String unit;
    @NotBlank
    private final String modelVersion;
    @NotBlank
    private final String disclaimer;

}
