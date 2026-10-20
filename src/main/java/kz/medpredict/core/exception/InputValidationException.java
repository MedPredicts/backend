package kz.medpredict.core.exception;

import java.util.List;
import kz.medpredict.core.exception.dto.FieldValidationErrorResponse;
import kz.medpredict.core.i18n.MessageCode;
import kz.medpredict.core.util.ErrorCode;
import lombok.Getter;

@Getter
public class InputValidationException extends ServiceValidationException {
    private final List<FieldValidationErrorResponse> fields;

    public InputValidationException(List<FieldValidationErrorResponse> fields) {
        super(ErrorCode.VALIDATION_ERROR, MessageCode.VALIDATION_FAILED);
        this.fields = List.copyOf(fields);
    }

}
