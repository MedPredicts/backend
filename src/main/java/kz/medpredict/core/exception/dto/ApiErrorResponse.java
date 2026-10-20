package kz.medpredict.core.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Schema(description = "Stable machine-readable error code with a localized safe message.")
public final class ApiErrorResponse {
    @NotBlank
    @Schema(description = "Stable machine-readable ErrorCode name; not localized.", allowableValues = {"INVALID_REQUEST", "MODEL_UNAVAILABLE", "SYSTEM_ERROR", "VALIDATION_ERROR"})
    private final String error;
    @NotBlank
    @Schema(description = "Localized safe text selected by Accept-Language, falling back to the configured default.")
    private final String message;

}
