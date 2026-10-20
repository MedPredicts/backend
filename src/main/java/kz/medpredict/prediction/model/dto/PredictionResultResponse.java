package kz.medpredict.prediction.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Schema(description = "A finite prediction with the version actually used and an educational disclaimer.")
public final class PredictionResultResponse {
    @Schema(description = "Finite numeric output; the demo profile always returns 134.2.")
    private final double predictedValue;
    @NotBlank
    @Schema(description = "Unit supplied by the active metadata; not inferred or translated automatically.")
    private final String unit;
    @NotBlank
    @Schema(description = "Version of the provider or artifact; prediction results use the version actually executed.")
    private final String modelVersion;
    @NotBlank
    @Schema(description = "Educational limitations, including explicit labeling for fixed demo responses.")
    private final String disclaimer;

}
