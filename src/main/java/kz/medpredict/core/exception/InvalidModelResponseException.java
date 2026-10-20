package kz.medpredict.core.exception;

import kz.medpredict.core.i18n.MessageCode;
import kz.medpredict.core.util.ErrorCode;

public class InvalidModelResponseException extends ServiceValidationException {
    public InvalidModelResponseException() {
        super(ErrorCode.MODEL_UNAVAILABLE, MessageCode.MODEL_INVALID_RESPONSE);
    }
}
