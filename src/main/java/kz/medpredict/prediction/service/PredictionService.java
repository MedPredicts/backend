package kz.medpredict.prediction.service;

import kz.medpredict.prediction.model.dto.PredictionCreateRequest;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.model.dto.PredictionModelStatusResponse;

public interface PredictionService {
    PredictionModelInfoResponse modelInfo();
    PredictionResultResponse predict(PredictionCreateRequest request);
    PredictionModelStatusResponse modelStatus();
}
