package kz.medpredict.prediction.service.impl;

import kz.medpredict.core.exception.InputValidationException;
import kz.medpredict.core.exception.InvalidModelResponseException;
import kz.medpredict.prediction.model.PredictionModel;
import kz.medpredict.prediction.model.dto.Field;
import kz.medpredict.prediction.model.dto.ModelInfo;
import kz.medpredict.prediction.model.dto.Prediction;
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
    private final PredictionService service = new PredictionServiceImpl(model, validator);
    private final ModelInfo metadata = new ModelInfo(
            "Synthetic target", "units", 1, "test-v1", List.of(
                    new Field("value", "Value", "number", true,
                            "units", 0.0, 10.0, true, null)));

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
                new Prediction(Double.NaN, "units", "test-v1", "Test"),
                new Prediction(5, "wrong", "test-v1", "Test"),
                new Prediction(5, "units", "", "Test"),
                new Prediction(5, "units", "test-v1", ""))) {
            when(model.predict(anyMap())).thenReturn(result);
            assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InvalidModelResponseException.class);
        }
        when(model.predict(anyMap())).thenReturn(null);
        assertThatThrownBy(() -> service.predict(body)).isInstanceOf(InvalidModelResponseException.class);
    }

    @Test
    void preservesVersionActuallyReturnedByModel() throws Exception {
        when(model.info()).thenReturn(metadata);
        var result = new Prediction(5, "units", "actual-v2", "Test");
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
