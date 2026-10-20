package kz.medpredict.core.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException() {
        super("Expected a JSON object containing only supported fields.");
    }
}
