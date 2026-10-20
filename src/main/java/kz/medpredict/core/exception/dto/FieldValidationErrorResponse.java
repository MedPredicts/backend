package kz.medpredict.core.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Schema(description = "An input field name and a localized validation message.")
public final class FieldValidationErrorResponse {
    @NotBlank
    @Schema(description = "The known input key to which this error belongs.")
    private final String field;
    @NotBlank
    @Schema(description = "Localized safe text selected by Accept-Language, falling back to the configured default.")
    private final String message;

}
