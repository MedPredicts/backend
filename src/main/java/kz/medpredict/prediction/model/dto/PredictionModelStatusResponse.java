package kz.medpredict.prediction.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Safe startup model status without internal paths or exception details.")
public final class PredictionModelStatusResponse {
    private final boolean ready;
    private final String status;
    private final String modelVersion;
    private final boolean demo;
}
