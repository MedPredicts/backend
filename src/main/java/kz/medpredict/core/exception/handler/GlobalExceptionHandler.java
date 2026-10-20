package kz.medpredict.core.exception.handler;

import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.core.exception.dto.ApiErrorResponse;
import kz.medpredict.core.exception.dto.ApiValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InputValidationException.class)
    ResponseEntity<ApiValidationErrorResponse> validation(InputValidationException exception) {
        return ResponseEntity.badRequest().body(
                new ApiValidationErrorResponse("VALIDATION_ERROR", exception.getFields()));
    }

    @ExceptionHandler({InvalidRequestException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiErrorResponse> malformed() {
        return ResponseEntity.badRequest().body(new ApiErrorResponse("INVALID_REQUEST",
                "Expected a JSON object with supported fields and valid values."));
    }

    @ExceptionHandler(ModelUnavailableException.class)
    ResponseEntity<ApiErrorResponse> unavailable() {
        return unavailableResponse("No trained model is configured.");
    }

    @ExceptionHandler(InvalidModelResponseException.class)
    ResponseEntity<ApiErrorResponse> invalidModel() {
        return unavailableResponse("The model cannot produce a valid response.");
    }

    private ResponseEntity<ApiErrorResponse> unavailableResponse(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("MODEL_UNAVAILABLE", message));
    }
}
