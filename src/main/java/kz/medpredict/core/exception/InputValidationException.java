package kz.medpredict.core.exception;

import java.util.List;
import kz.medpredict.core.exception.dto.FieldValidationErrorResponse;

public class InputValidationException extends RuntimeException {
    private final List<FieldValidationErrorResponse> fields;

    public InputValidationException(List<FieldValidationErrorResponse> fields) {
        super("Prediction input does not match the model schema.");
        this.fields = List.copyOf(fields);
    }

    public List<FieldValidationErrorResponse> getFields() {
        return fields;
    }
}
