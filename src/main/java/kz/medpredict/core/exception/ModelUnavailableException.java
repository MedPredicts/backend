package kz.medpredict.core.exception;

import kz.medpredict.core.i18n.MessageCode;
import kz.medpredict.core.util.ErrorCode;

public class ModelUnavailableException extends ServiceValidationException {
    public ModelUnavailableException() {
        super(ErrorCode.MODEL_UNAVAILABLE, MessageCode.MODEL_UNAVAILABLE);
    }
}
