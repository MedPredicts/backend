package kz.medpredict.prediction.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kz.medpredict.core.i18n.MessageCode;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PredictionCreateRequestValidator.class)
public @interface ValidPredictionInput {
    String message() default MessageCode.INVALID_REQUEST;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
