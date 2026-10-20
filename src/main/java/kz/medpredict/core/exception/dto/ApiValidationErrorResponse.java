package kz.medpredict.core.exception.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@EqualsAndHashCode
public final class ApiValidationErrorResponse {
    @NotBlank
    private final String error;
    @NotEmpty @Valid
    private final List<FieldValidationErrorResponse> fields;

    // Copy collections so callers cannot mutate response state.
    public ApiValidationErrorResponse(String error, List<FieldValidationErrorResponse> fields) {
        this.error = error;
        this.fields = fields == null ? null : List.copyOf(fields);
    }
}
