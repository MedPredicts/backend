package kz.medpredict.prediction.infrastructure.model.impl;

import java.util.Map;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.model.dto.PredictionModelStatusResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!demo & !onnx")
public class UnavailablePredictionModel implements PredictionModel {
    @Override
    public PredictionModelStatusResponse status() {
        return new PredictionModelStatusResponse(false, "UNAVAILABLE", null, false);
    }
    @Override
    public PredictionModelInfoResponse info() {
        throw new ModelUnavailableException();
    }

    @Override
    public PredictionResultResponse predict(Map<String, Object> input) {
        throw new ModelUnavailableException();
    }
}
