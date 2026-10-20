package kz.medpredict.prediction.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@EqualsAndHashCode
public final class PredictionModelInfoResponse {
    @NotBlank
    private final String indicatorName;
    @NotBlank
    private final String unit;
    @Min(0) @Max(20)
    private final Integer decimalPlaces;
    private final String modelVersion;
    @NotEmpty @Valid
    private final List<PredictionFieldResponse> fields;

    // Copy collections so callers cannot mutate response state.
    public PredictionModelInfoResponse(String indicatorName, String unit, Integer decimalPlaces, String modelVersion, List<PredictionFieldResponse> fields) {
        this.indicatorName = indicatorName;
        this.unit = unit;
        this.decimalPlaces = decimalPlaces;
        this.modelVersion = modelVersion;
        this.fields = fields == null ? null : List.copyOf(fields);
    }
}
