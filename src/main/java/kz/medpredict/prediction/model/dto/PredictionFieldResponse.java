package kz.medpredict.prediction.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@EqualsAndHashCode
@Schema(description = "One model input: number or select. Constraints and units depend on its type.")
public final class PredictionFieldResponse {
    @NotBlank
    @Schema(description = "Unique input key: Latin letter followed by letters, digits or underscores; reserved names are forbidden.")
    private final String key;
    @NotBlank
    @Schema(description = "Provider-supplied display label.")
    private final String label;
    @NotBlank @Pattern(regexp = "number|select")
    @Schema(description = "Input kind: number or select.", allowableValues = {"number", "select"})
    private final String type;
    @Schema(description = "Whether the input must be submitted.")
    private final boolean required;
    @Schema(description = "Unit supplied by the active metadata; not inferred or translated automatically.")
    private final String unit;
    @Schema(description = "Optional approved inclusive minimum for number inputs.")
    private final Double min;
    @Schema(description = "Optional approved inclusive maximum for number inputs.")
    private final Double max;
    @Schema(description = "Optional integer-only rule for number inputs.")
    private final Boolean integer;
    @Valid
    @Schema(description = "Required nonempty unique categories for select inputs; absent for numeric fields.")
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
