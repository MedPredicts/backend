package kz.medpredict.prediction.model;

import java.util.Map;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;

/** Contract for a versioned model with matching input metadata. */
public interface PredictionModel {
    ModelInfo info();
    Prediction predict(Map<String, Object> input);
}
