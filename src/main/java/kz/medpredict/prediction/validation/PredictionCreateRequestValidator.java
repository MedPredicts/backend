package kz.medpredict.prediction.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.core.util.ErrorCode;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PredictionCreateRequestValidator implements ConstraintValidator<ValidPredictionInput, PredictionCreateRequest> {
    private final PredictionModel model;
    private final InputValidator inputValidator;
    private final ModelMetadataValidator metadataValidator;

    @Override
    public boolean isValid(PredictionCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Required request bodies are enforced by Spring MVC.
        }
        context.disableDefaultConstraintViolation();
        try {
            var metadata = model.info();
            metadataValidator.validate(metadata);
            inputValidator.validate(request.getValues(), metadata);
            return true;
        } catch (InputValidationException exception) {
            for (var error : exception.getFields()) {
                context.buildConstraintViolationWithTemplate(error.getMessage())
                        .addPropertyNode(error.getField()).addConstraintViolation();
            }
            return false;
        } catch (InvalidRequestException exception) {
            return globalViolation(context, ErrorCode.INVALID_REQUEST.name());
        } catch (ModelUnavailableException exception) {
            return globalViolation(context, ErrorCode.MODEL_UNAVAILABLE.name());
        }
    }

    private boolean globalViolation(ConstraintValidatorContext context, String code) {
        context.buildConstraintViolationWithTemplate(code).addConstraintViolation();
        return false;
    }
}
