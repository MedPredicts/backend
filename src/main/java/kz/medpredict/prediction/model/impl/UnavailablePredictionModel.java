package kz.medpredict.prediction.model.impl;

import java.util.Map;
import kz.medpredict.core.exception.ModelUnavailableException;
import kz.medpredict.prediction.model.PredictionModel;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!demo")
public class UnavailablePredictionModel implements PredictionModel {
    @Override
    public ModelInfo info() {
        throw new ModelUnavailableException();
    }

    @Override
    public Prediction predict(Map<String, Object> input) {
        throw new ModelUnavailableException();
    }
}
