package kz.medpredict.prediction.infrastructure.model;

import java.util.Map;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.model.dto.PredictionModelStatusResponse;

/** Contract for a versioned model with matching input metadata. */
public interface PredictionModel {
    PredictionModelInfoResponse info();
    PredictionResultResponse predict(Map<String, Object> input);
    default PredictionModelStatusResponse status() {
        var metadata = info();
        boolean demo = Boolean.TRUE.equals(metadata.getDemo());
        return new PredictionModelStatusResponse(true, demo ? "DEMO" : "READY", metadata.getModelVersion(), demo);
    }
}
