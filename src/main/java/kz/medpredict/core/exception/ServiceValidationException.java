package kz.medpredict.core.exception;

import java.util.Objects;
import kz.medpredict.core.util.ErrorCode;
import lombok.Getter;

/** An application failure identified by a stable error code and a localizable message key. */
public class ServiceValidationException extends RuntimeException {
    @Getter
    private final ErrorCode errorCode;
    private final Object[] arguments;

    public ServiceValidationException(ErrorCode errorCode, String messageCode, Object... arguments) {
        super(Objects.requireNonNull(messageCode, "A message code is required."));
        this.errorCode = Objects.requireNonNull(errorCode, "An error code is required.");
        this.arguments = arguments == null ? new Object[0] : arguments.clone();
    }

    public Object[] getArguments() {
        return arguments.clone();
    }
}
