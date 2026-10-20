package kz.medpredict.prediction.validation;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.core.exception.dto.FieldError;
import kz.medpredict.prediction.model.dto.Field;
import kz.medpredict.prediction.model.dto.ModelInfo;
import org.springframework.stereotype.Component;

@Component
public class InputValidator {
    public Map<String, Object> validate(JsonNode body, ModelInfo metadata) {
        validateObjectAndKeys(body, metadata);
        List<FieldError> errors = new ArrayList<>();
        Map<String, Object> values = new LinkedHashMap<>();
        for (var field : metadata.fields()) {
            JsonNode value = body.get(field.key());
            if (value == null) {
                if (field.required()) {
                    errors.add(new FieldError(field.key(), "Обязательное поле"));
                }
                continue;
            }
            String error = validationError(value, field);
            if (error != null) {
                errors.add(new FieldError(field.key(), error));
            } else {
                values.put(field.key(), value.isNumber() ? value.doubleValue() : value.textValue());
            }
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }
        return Collections.unmodifiableMap(values);
    }

    private void validateObjectAndKeys(JsonNode body, ModelInfo metadata) {
        if (body == null || !body.isObject()) {
            throw new InvalidRequestException();
        }
        Set<String> knownKeys = metadata.fields().stream()
                .map(Field::key).collect(Collectors.toSet());
        var keys = body.fieldNames();
        while (keys.hasNext()) {
            if (!knownKeys.contains(keys.next())) {
                // The frontend cannot attach field errors to unknown names.
                throw new InvalidRequestException();
            }
        }
    }

    private String validationError(JsonNode value, Field field) {
        return switch (field.type()) {
            case "number" -> numberError(value, field);
            case "select" -> selectError(value, field);
            default -> throw new InvalidModelResponseException();
        };
    }

    private String numberError(JsonNode node, Field field) {
        if (!node.isNumber() || !Double.isFinite(node.doubleValue())) {
            return "Ожидается конечное число";
        }
        double value = node.doubleValue();
        boolean belowMinimum = field.min() != null && value < field.min();
        boolean aboveMaximum = field.max() != null && value > field.max();
        boolean fractionalInteger = Boolean.TRUE.equals(field.integer()) && value != Math.rint(value);
        return belowMinimum || aboveMaximum || fractionalInteger
                ? "Значение не соответствует ограничениям поля" : null;
    }

    private String selectError(JsonNode value, Field field) {
        boolean allowed = value.isTextual()
                && field.options().stream().anyMatch(option -> option.value().equals(value.textValue()));
        return allowed ? null : "Выберите допустимое значение";
    }
}
