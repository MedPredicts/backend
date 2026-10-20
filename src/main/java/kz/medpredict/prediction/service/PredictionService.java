package kz.medpredict.prediction.service;

import kz.medpredict.prediction.model.dto.PredictionCreateRequest;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;

public interface PredictionService {
    PredictionModelInfoResponse modelInfo();
    PredictionResultResponse predict(PredictionCreateRequest request);
}
