package kz.medpredict.prediction.service.impl;

import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.service.PredictionService;
import kz.medpredict.prediction.validation.InputValidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PredictionServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final PredictionModel model = mock(PredictionModel.class);
    private final InputValidator validator = new InputValidator();
    private final jakarta.validation.ValidatorFactory validatorFactory = jakarta.validation.Validation.buildDefaultValidatorFactory();
    private final PredictionService service = new PredictionServiceImpl(model, validator, validatorFactory.getValidator());

    @org.junit.jupiter.api.AfterEach
    void closeValidatorFactory() {
        validatorFactory.close();
    }
    private final PredictionModelInfoResponse metadata = new PredictionModelInfoResponse(
            "Synthetic target", "units", 1, "test-v1", List.of(
                    new PredictionFieldResponse("value", "Value", "number", true,
                            "units", 0.0, 10.0, true, null)));

    @Test
    void rejectsMetadataWithInvalidNestedDtoBeforeInference() throws Exception {
        when(model.info()).thenReturn(new PredictionModelInfoResponse("Target", "units", 1, "v1", List.of(
                new PredictionFieldResponse("", "Value", "number", true, "units", null, null, null, null))));
        var body = mapper.readTree("{}");
        assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InvalidModelResponseException.class);
        verify(model, never()).predict(anyMap());
    }

    @Test
    void invalidInputNeverReachesModel() throws Exception {
        when(model.info()).thenReturn(metadata);
        var body = mapper.readTree("{\"value\":11}");
        assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InputValidationException.class);
        verify(model, never()).predict(anyMap());
    }

    @Test
    void rejectsInvalidModelOutputs() throws Exception {
        when(model.info()).thenReturn(metadata);
        var body = mapper.readTree("{\"value\":5}");
        for (var result : List.of(
                new PredictionResultResponse(Double.NaN, "units", "test-v1", "Test"),
                new PredictionResultResponse(5, "wrong", "test-v1", "Test"),
                new PredictionResultResponse(5, "units", "", "Test"),
                new PredictionResultResponse(5, "units", "test-v1", ""))) {
            when(model.predict(anyMap())).thenReturn(result);
            assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InvalidModelResponseException.class);
        }
        when(model.predict(anyMap())).thenReturn(null);
        assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InvalidModelResponseException.class);
    }

    @Test
    void preservesVersionActuallyReturnedByModel() throws Exception {
        when(model.info()).thenReturn(metadata);
        var result = new PredictionResultResponse(5, "units", "actual-v2", "Test");
        when(model.predict(anyMap())).thenReturn(result);
        assertThat(service.predict(mapper.readTree("{\"value\":5}"))).isEqualTo(result);
    }

    @Test
    void enforcesInclusiveBoundsAndIntegerConstraint() throws Exception {
        for (String value : List.of("-1", "11", "1.5")) {
            var body = mapper.readTree("{\"value\":" + value + "}");
            assertThatThrownBy(() -> validator.validate(body, metadata))
                    .isInstanceOf(InputValidationException.class);
        }
        for (String value : List.of("0", "10")) {
            assertThat(validator.validate(mapper.readTree("{\"value\":" + value + "}"), metadata))
                    .containsKey("value");
        }
    }
}

