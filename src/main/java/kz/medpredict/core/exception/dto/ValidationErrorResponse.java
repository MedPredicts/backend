package kz.medpredict.core.exception.dto;

import java.util.List;

public record ValidationErrorResponse(String error, List<FieldError> fields) {
    public ValidationErrorResponse {
        fields = List.copyOf(fields);
    }
}
