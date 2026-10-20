package kz.medpredict.prediction.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@EqualsAndHashCode
public final class PredictionFieldResponse {
    @NotBlank
    private final String key;
    @NotBlank
    private final String label;
    @NotBlank @Pattern(regexp = "number|select")
    private final String type;
    private final boolean required;
    private final String unit;
    private final Double min;
    private final Double max;
    private final Boolean integer;
    @Valid
    private final List<PredictionFieldOptionResponse> options;

    public PredictionFieldResponse(String key, String label, String type, boolean required, String unit, Double min, Double max, Boolean integer, List<PredictionFieldOptionResponse> options) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.integer = integer;
        this.options = options == null ? null : List.copyOf(options);
    }
}
