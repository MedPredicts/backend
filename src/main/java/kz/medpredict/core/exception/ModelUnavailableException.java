package kz.medpredict.core.exception;

public class ModelUnavailableException extends RuntimeException {
    public ModelUnavailableException() {
        super("No trained model is configured.");
    }
}
