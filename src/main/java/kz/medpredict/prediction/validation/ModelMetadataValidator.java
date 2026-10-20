package kz.medpredict.prediction.validation;

import jakarta.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelMetadataValidator {
    private static final Pattern FIELD_KEY = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
    private static final Set<String> RESERVED_KEYS = Set.of("constructor", "prototype", "__proto__");
    private final Validator beanValidator;

    public void validate(PredictionModelInfoResponse metadata) {
        if (metadata == null || !beanValidator.validate(metadata).isEmpty()) {
            throw new InvalidModelResponseException();
        }
        Set<String> keys = new HashSet<>();
        for (var field : metadata.getFields()) {
            if (field == null || !FIELD_KEY.matcher(field.getKey()).matches()
                    || RESERVED_KEYS.contains(field.getKey()) || !keys.add(field.getKey())) {
                throw new InvalidModelResponseException();
            }
            if ("number".equals(field.getType())) {
                validateNumber(field);
            } else {
                validateOptions(field);
            }
        }
    }

    private void validateNumber(PredictionFieldResponse field) {
        if (field.getUnit() == null || field.getUnit().isBlank()
                || !finiteOrAbsent(field.getMin()) || !finiteOrAbsent(field.getMax())
                || (field.getMin() != null && field.getMax() != null && field.getMin() > field.getMax())) {
            throw new InvalidModelResponseException();
        }
    }

    private void validateOptions(PredictionFieldResponse field) {
        if (field.getOptions() == null || field.getOptions().isEmpty()) {
            throw new InvalidModelResponseException();
        }
        Set<String> values = new HashSet<>();
        for (var option : field.getOptions()) {
            if (option == null || !values.add(option.getValue())) {
                throw new InvalidModelResponseException();
            }
        }
    }

    private boolean finiteOrAbsent(Double value) {
        return value == null || Double.isFinite(value);
    }
}
