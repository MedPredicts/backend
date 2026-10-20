package kz.medpredict.prediction.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.prediction.model.PredictionModel;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;
import kz.medpredict.prediction.service.PredictionService;
import kz.medpredict.prediction.validation.InputValidator;
import org.springframework.stereotype.Service;

@Service
public class PredictionServiceImpl implements PredictionService {
    private final PredictionModel model;
    private final InputValidator inputValidator;

    public PredictionServiceImpl(PredictionModel model, InputValidator inputValidator) {
        this.model = model;
        this.inputValidator = inputValidator;
    }

    @Override
    public ModelInfo modelInfo() {
        return model.info();
    }

    @Override
    public Prediction predict(JsonNode body) {
        var metadata = model.info();
        var input = inputValidator.validate(body, metadata);
        var prediction = model.predict(input);
        validatePrediction(prediction, metadata.unit());
        return prediction;
    }

    private void validatePrediction(Prediction prediction, String expectedUnit) {
        if (prediction == null || !Double.isFinite(prediction.predictedValue())
                || !expectedUnit.equals(prediction.unit())
                || isBlank(prediction.modelVersion()) || isBlank(prediction.disclaimer())) {
            throw new InvalidModelResponseException();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
