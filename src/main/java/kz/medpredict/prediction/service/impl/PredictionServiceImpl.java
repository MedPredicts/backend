package kz.medpredict.prediction.service.impl;

import jakarta.validation.Validator;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.core.exception.InvalidRequestException;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionCreateRequest;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.service.PredictionService;
import kz.medpredict.prediction.validation.InputValidator;
import kz.medpredict.prediction.validation.ModelMetadataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {
    private final PredictionModel model;
    private final InputValidator inputValidator;
    private final Validator beanValidator;
    private final ModelMetadataValidator metadataValidator;

    @Override
    public PredictionModelInfoResponse modelInfo() {
        var metadata = model.info();
        metadataValidator.validate(metadata);
        return metadata;
    }

    @Override
    public PredictionResultResponse predict(PredictionCreateRequest request) {
        if (request == null) {
            throw new InvalidRequestException();
        }
        var metadata = modelInfo();
        var input = inputValidator.validate(request.getValues(), metadata);
        var prediction = model.predict(input);
        validatePrediction(prediction, metadata.getUnit());
        return prediction;
    }

    private void validatePrediction(PredictionResultResponse prediction, String expectedUnit) {
        if (prediction == null || !Double.isFinite(prediction.getPredictedValue())
                || !expectedUnit.equals(prediction.getUnit())
                || !beanValidator.validate(prediction).isEmpty()) {
            throw new InvalidModelResponseException();
        }
    }

}
