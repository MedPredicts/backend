package kz.medpredict.core.exception.handler;

import java.util.List;
import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.ServiceValidationException;
import kz.medpredict.core.exception.dto.ApiErrorResponse;
import kz.medpredict.core.exception.dto.ApiValidationErrorResponse;
import kz.medpredict.core.exception.dto.FieldValidationErrorResponse;
import kz.medpredict.core.i18n.MessageCode;
import kz.medpredict.core.i18n.MessageService;
import kz.medpredict.core.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final MessageService messageService;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var binding = exception.getBindingResult();
        if (binding.getGlobalErrors().stream()
                .anyMatch(error -> ErrorCode.MODEL_UNAVAILABLE.name().equals(error.getDefaultMessage()))) {
            return response(ErrorCode.MODEL_UNAVAILABLE, MessageCode.MODEL_INCOMPATIBLE);
        }
        if (binding.hasGlobalErrors()) {
            return response(ErrorCode.INVALID_REQUEST, MessageCode.INVALID_REQUEST);
        }
        var fields = binding.getFieldErrors().stream()
                .map(error -> new FieldValidationErrorResponse(error.getField(),
                        messageService.getMessage(error.getDefaultMessage())))
                .toList();
        return fieldErrors(fields);
    }

    @ExceptionHandler(InputValidationException.class)
    ResponseEntity<Object> validation(InputValidationException exception) {
        return fieldErrors(exception.getFields().stream()
                .map(error -> new FieldValidationErrorResponse(error.getField(),
                        messageService.getMessage(error.getMessage())))
                .toList());
    }

    @ExceptionHandler(ServiceValidationException.class)
    ResponseEntity<ApiErrorResponse> serviceValidation(ServiceValidationException exception) {
        return ResponseEntity.status(exception.getErrorCode().getStatus())
                .body(errorBody(exception.getErrorCode(), exception.getMessage(), exception.getArguments()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Preserve framework status and headers (e.g. Allow), but never expose its exception detail.
        ErrorCode code = status.is5xxServerError() ? ErrorCode.SYSTEM_ERROR : ErrorCode.INVALID_REQUEST;
        String message = status.is5xxServerError() ? MessageCode.SYSTEM_ERROR : MessageCode.INVALID_REQUEST;
        return new ResponseEntity<>(errorBody(code, message), headers, status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception exception) {
        // Bean Validation can wrap provider exceptions. Unwrap only recognized application failures.
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 20; depth++, cause = cause.getCause()) {
            if (cause instanceof InputValidationException input) {
                return validation(input);
            }
            if (cause instanceof ServiceValidationException service) {
                return response(service.getErrorCode(), service.getMessage(), service.getArguments());
            }
        }
        return response(ErrorCode.SYSTEM_ERROR, MessageCode.SYSTEM_ERROR);
    }

    private ResponseEntity<Object> fieldErrors(List<FieldValidationErrorResponse> fields) {
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(new ApiValidationErrorResponse(ErrorCode.VALIDATION_ERROR.name(), fields));
    }

    private ResponseEntity<Object> response(ErrorCode code, String messageCode, Object... arguments) {
        return ResponseEntity.status(code.getStatus()).body(errorBody(code, messageCode, arguments));
    }

    private ApiErrorResponse errorBody(ErrorCode code, String messageCode, Object... arguments) {
        return new ApiErrorResponse(code.name(), messageService.getMessage(messageCode, arguments));
    }
}
