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
import kz.medpredict.core.exception.dto.FieldValidationErrorResponse;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import org.springframework.stereotype.Component;

@Component
public class InputValidator {
    public Map<String, Object> validate(JsonNode body, PredictionModelInfoResponse metadata) {
        validateObjectAndKeys(body, metadata);
        List<FieldValidationErrorResponse> errors = new ArrayList<>();
        Map<String, Object> values = new LinkedHashMap<>();
        for (var field : metadata.getFields()) {
            JsonNode value = body.get(field.getKey());
            if (value == null) {
                if (field.isRequired()) {
                    errors.add(new FieldValidationErrorResponse(field.getKey(), "Обязательное поле"));
                }
                continue;
            }
            String error = validationError(value, field);
            if (error != null) {
                errors.add(new FieldValidationErrorResponse(field.getKey(), error));
            } else {
                values.put(field.getKey(), value.isNumber() ? value.doubleValue() : value.textValue());
            }
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }
        return Collections.unmodifiableMap(values);
    }

    private void validateObjectAndKeys(JsonNode body, PredictionModelInfoResponse metadata) {
        if (body == null || !body.isObject()) {
            throw new InvalidRequestException();
        }
        Set<String> knownKeys = metadata.getFields().stream()
                .map(PredictionFieldResponse::getKey).collect(Collectors.toSet());
        var keys = body.fieldNames();
        while (keys.hasNext()) {
            if (!knownKeys.contains(keys.next())) {
                // The frontend cannot attach field errors to unknown names.
                throw new InvalidRequestException();
            }
        }
    }

    private String validationError(JsonNode value, PredictionFieldResponse field) {
        return switch (field.getType()) {
            case "number" -> numberError(value, field);
            case "select" -> selectError(value, field);
            default -> throw new InvalidModelResponseException();
        };
    }

    private String numberError(JsonNode node, PredictionFieldResponse field) {
        if (!node.isNumber() || !Double.isFinite(node.doubleValue())) {
            return "Ожидается конечное число";
        }
        double value = node.doubleValue();
        boolean belowMinimum = field.getMin() != null && value < field.getMin();
        boolean aboveMaximum = field.getMax() != null && value > field.getMax();
        boolean fractionalInteger = Boolean.TRUE.equals(field.getInteger()) && value != Math.rint(value);
        return belowMinimum || aboveMaximum || fractionalInteger
                ? "Значение не соответствует ограничениям поля" : null;
    }

    private String selectError(JsonNode value, PredictionFieldResponse field) {
        boolean allowed = value.isTextual()
                && field.getOptions().stream().anyMatch(option -> option.getValue().equals(value.textValue()));
        return allowed ? null : "Выберите допустимое значение";
    }
}
