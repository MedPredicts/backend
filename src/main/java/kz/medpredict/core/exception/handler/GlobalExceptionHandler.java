package kz.medpredict.core.exception.handler;

import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.core.exception.dto.ErrorResponse;
import kz.medpredict.core.exception.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InputValidationException.class)
    ResponseEntity<ValidationErrorResponse> validation(InputValidationException exception) {
        return ResponseEntity.badRequest().body(
                new ValidationErrorResponse("VALIDATION_ERROR", exception.fields()));
    }

    @ExceptionHandler({InvalidRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> malformed() {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST",
                "Expected a JSON object with supported fields and valid values."));
    }

    @ExceptionHandler(ModelUnavailableException.class)
    ResponseEntity<ErrorResponse> unavailable() {
        return unavailableResponse("No trained model is configured.");
    }

    @ExceptionHandler(InvalidModelResponseException.class)
    ResponseEntity<ErrorResponse> invalidModel() {
        return unavailableResponse("The model cannot produce a valid response.");
    }

    private ResponseEntity<ErrorResponse> unavailableResponse(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("MODEL_UNAVAILABLE", message));
    }
}
