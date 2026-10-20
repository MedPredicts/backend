package kz.medpredict.prediction.infrastructure.model;

import java.util.Map;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;

/** Contract for a versioned model with matching input metadata. */
public interface PredictionModel {
    PredictionModelInfoResponse info();
    PredictionResultResponse predict(Map<String, Object> input);
}

