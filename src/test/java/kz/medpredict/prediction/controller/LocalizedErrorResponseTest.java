package kz.medpredict.prediction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class LocalizedErrorResponseTest {
    @Test
    void frameworkErrorsKeepTheirHttpStatusAndUseLocalizedBody() throws Exception {
        mvc.perform(put("/api/v1/predictions").header("Accept-Language", "en"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Expected a JSON object with supported fields and valid values."));
        mvc.perform(post("/api/v1/predictions").contentType("text/plain")
                        .header("Accept-Language", "ru").content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Ожидается JSON-объект с поддерживаемыми полями и допустимыми значениями."));
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void translatesFieldErrorsForAllSupportedLanguages() throws Exception {
        for (var example : new String[][]{
                {"ru", "Обязательное поле."}, {"en", "This field is required."},
                {"kk", "Бұл өріс міндетті."}, {"en-US", "This field is required."},
                {"fr", "Обязательное поле."}, {"fr;q=1,en;q=0.8", "This field is required."}}) {
            mvc.perform(post("/api/v1/predictions").contentType("application/json")
                            .header("Accept-Language", example[0]).content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fields[*].message", hasItem(example[1])));
        }
    }

    @Test
    void translatesMalformedRequestsAndUsesDefaultWithoutHeader() throws Exception {
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
                        .header("Accept-Language", "en").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Expected a JSON object with supported fields and valid values."));
        mvc.perform(post("/api/v1/predictions").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].message", hasItem("Обязательное поле.")));
    }
}
