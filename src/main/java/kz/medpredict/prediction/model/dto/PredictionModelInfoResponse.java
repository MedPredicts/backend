package kz.medpredict.prediction.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Schema(description = "Active model metadata used to render and validate the input form.")
public final class PredictionModelInfoResponse {
    @NotBlank
    @Schema(description = "Target display name supplied by the model provider.")
    private final String indicatorName;
    @NotBlank
    @Schema(description = "Unit supplied by the active metadata; not inferred or translated automatically.")
    private final String unit;
    @Min(0) @Max(20)
    @Schema(description = "Display precision from 0 to 20; explicit null preserves original precision.", nullable = true, requiredMode = Schema.RequiredMode.REQUIRED)
    private final Integer decimalPlaces;
    @Schema(description = "Version of the provider or artifact; prediction results use the version actually executed.")
    private final String modelVersion;
    @NotEmpty @Valid
    @Schema(description = "Ordered input definitions for metadata, or known-field violations for error responses.")
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
