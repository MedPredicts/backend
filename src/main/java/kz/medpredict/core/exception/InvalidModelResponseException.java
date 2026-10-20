package kz.medpredict.core.exception;

public class InvalidModelResponseException extends RuntimeException {
    public InvalidModelResponseException() {
        super("The model returned an invalid response.");
    }
}
