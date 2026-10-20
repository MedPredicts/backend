package kz.medpredict.prediction.model.dto;

import java.util.List;

public record ModelInfo(
        String indicatorName,
        String unit,
        Integer decimalPlaces,
        String modelVersion,
        List<Field> fields) {
    public ModelInfo {
        fields = List.copyOf(fields);
    }
}
