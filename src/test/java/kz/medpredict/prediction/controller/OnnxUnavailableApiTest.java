package kz.medpredict.prediction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "medpredict.model.directory=${java.io.tmpdir}/medpredict-test-intentionally-missing-model")
@AutoConfigureMockMvc
@ActiveProfiles("onnx")
class OnnxUnavailableApiTest {
    @Autowired MockMvc mvc;

    @Test
    void serverStartsButIsNotReadyAndNeverReturnsFakePrediction() throws Exception {
        mvc.perform(get("/api/v1/health/live")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/model/status")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.ready").value(false)).andExpect(jsonPath("$.demo").value(false));
        mvc.perform(get("/api/v1/model/info")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"));
        mvc.perform(post("/api/v1/predictions").contentType("application/json").content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"))
                .andExpect(jsonPath("$.predictedValue").doesNotExist());
    }
}
