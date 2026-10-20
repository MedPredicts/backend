package kz.medpredict.prediction.service;

import com.fasterxml.jackson.databind.JsonNode;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;

public interface PredictionService {
    ModelInfo modelInfo();
    Prediction predict(JsonNode body);
}
