package kz.medpredict.prediction.model.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class PredictionFieldOptionResponse {
    @NotBlank
    private final String value;
    @NotBlank
    private final String label;

}
