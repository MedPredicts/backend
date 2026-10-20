package kz.medpredict.prediction.controller;

import java.util.List;
import java.util.Map;
import kz.medpredict.prediction.infrastructure.model.PredictionModel;
import kz.medpredict.prediction.model.dto.PredictionFieldResponse;
import kz.medpredict.prediction.model.dto.PredictionModelInfoResponse;
import kz.medpredict.prediction.model.dto.PredictionResultResponse;
import kz.medpredict.prediction.service.PredictionService;
import kz.medpredict.core.exception.InvalidModelResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PredictionCreateRequestValidationTest {
    @Test
    void wrappedModelFailuresKeepTheirLocalizedStatusAndCode() throws Exception {
        when(model.info()).thenThrow(new InvalidModelResponseException());
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .header("Accept-Language", "en").content("{\"measurement\":5}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("The model cannot produce a valid response."));
    }

    @Test
    void unexpectedFailuresAreLocalizedWithoutLeakingExceptionText() throws Exception {
        when(model.info()).thenThrow(new IllegalStateException("secret input must not escape"));
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .header("Accept-Language", "en").content("{\"measurement\":5}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("SYSTEM_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred."));
    }

    @Test
    void invalidMetadataIsUnavailableBeforeInputValidation() throws Exception {
        when(model.info()).thenReturn(new PredictionModelInfoResponse("Target", "units", 1, "test-v1",
                List.of(new PredictionFieldResponse("category", "Category", "select", true,
                        null, null, null, null, null))));
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .header("Accept-Language", "kk").content("{\"category\":\"a\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Модель дұрыс жауап бере алмайды."));
        verify(model, never()).predict(anyMap());
    }

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private PredictionModel model;
    @MockitoSpyBean
    private PredictionService service;

    @BeforeEach
    void configureDifferentModelSchema() {
        when(model.info()).thenReturn(new PredictionModelInfoResponse("Synthetic target", "units", 1, "test-v1",
                List.of(new PredictionFieldResponse("measurement", "Measurement", "number", true,
                                "units", 0.0, 10.0, true, null),
                        new PredictionFieldResponse("optionalValue", "Optional value", "number", false,
                                "units", null, null, null, null))));
        when(model.predict(anyMap())).thenReturn(new PredictionResultResponse(5, "units", "test-v1", "Test"));
    }

    @Test
    void acceptsModelSpecificFieldsWithoutPayloadEnvelope() throws Exception {
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .content("{\"measurement\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.predictedValue").value(5));
        verify(model).predict(Map.of("measurement", 5.0));
    }

    @Test
    void beanValidationRejectsInvalidModelFieldsBeforeServiceInvocation() throws Exception {
        for (String body : List.of("{}", "{\"measurement\":11}", "{\"measurement\":1.5}",
                "{\"measurement\":\"5\"}", "{\"measurement\":null}")) {
            mvc.perform(post("/api/v1/predictions").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fields[0].field").value("measurement"));
        }
        verify(service, never()).predict(any());
        verify(model, never()).predict(anyMap());
    }

    @Test
    void unknownFieldsRemainGeneralRequestErrors() throws Exception {
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .content("{\"measurement\":5,\"age\":52}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        verify(service, never()).predict(any());
    }
}
