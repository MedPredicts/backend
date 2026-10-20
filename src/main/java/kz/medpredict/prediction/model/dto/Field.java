package kz.medpredict.prediction.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Field(
        String key,
        String label,
        String type,
        boolean required,
        String unit,
        Double min,
        Double max,
        Boolean integer,
        List<Option> options) {
    public Field {
        options = options == null ? null : List.copyOf(options);
    }
}
