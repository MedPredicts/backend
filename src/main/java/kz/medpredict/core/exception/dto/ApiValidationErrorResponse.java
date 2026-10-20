package kz.medpredict.core.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Schema(description = "Known-field validation errors; field names remain stable across languages.")
public final class ApiValidationErrorResponse {
    @NotBlank
    @Schema(description = "Stable machine-readable ErrorCode name; not localized.", allowableValues = {"VALIDATION_ERROR"})
    private final String error;
    @NotEmpty @Valid
    @Schema(description = "Ordered input definitions for metadata, or known-field violations for error responses.")
    private final List<FieldValidationErrorResponse> fields;

    // Copy collections so callers cannot mutate response state.
    public ApiValidationErrorResponse(String error, List<FieldValidationErrorResponse> fields) {
        this.error = error;
        this.fields = fields == null ? null : List.copyOf(fields);
    }
}
