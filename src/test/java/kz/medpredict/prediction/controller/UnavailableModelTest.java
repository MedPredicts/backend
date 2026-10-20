package kz.medpredict.prediction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UnavailableModelTest {
    @Test
    void unavailableModelErrorIsLocalized() throws Exception {
        mvc.perform(get("/api/v1/model/info").header("Accept-Language", "en"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("No trained model is configured."));
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .header("Accept-Language", "kk").content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Үйлесімді модель қолжетімсіз."));
    }

    @Autowired MockMvc mvc;
    @Test void noSilentDemoFallback() throws Exception {
        mvc.perform(get("/api/v1/model/info")).andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"));
        mvc.perform(post("/api/v1/predictions").contentType("application/json").content("{}"))
            .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.error").value("MODEL_UNAVAILABLE"));
    }
}
