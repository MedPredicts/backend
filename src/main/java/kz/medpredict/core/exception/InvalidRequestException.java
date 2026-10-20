package kz.medpredict.core.exception;

import kz.medpredict.core.i18n.MessageCode;
import kz.medpredict.core.util.ErrorCode;

public class InvalidRequestException extends ServiceValidationException {
    public InvalidRequestException() {
        super(ErrorCode.INVALID_REQUEST, MessageCode.INVALID_REQUEST);
    }
}
