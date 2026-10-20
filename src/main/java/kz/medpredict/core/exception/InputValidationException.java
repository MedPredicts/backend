package kz.medpredict.core.exception;

import java.util.List;
import kz.medpredict.core.exception.dto.FieldError;

public class InputValidationException extends RuntimeException {
    private final List<FieldError> fields;

    public InputValidationException(List<FieldError> fields) {
        super("Prediction input does not match the model schema.");
        this.fields = List.copyOf(fields);
    }

    public List<FieldError> fields() {
        return fields;
    }
}
