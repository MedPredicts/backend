package kz.medpredict.prediction.service;

import com.fasterxml.jackson.databind.JsonNode;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;

public interface PredictionService {
    PredictionModelInfoResponse modelInfo();
    PredictionResultResponse predict(JsonNode body);
}
