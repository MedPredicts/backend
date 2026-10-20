package kz.medpredict.prediction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class PredictionApiTest {
    @Autowired MockMvc mvc;
    private static final String VALID = "{\"age\":52,\"sex\":\"male\",\"bmi\":27.4,\"totalCholesterol\":5.6,\"glucose\":5.9}";
    @Test void metadataAndPredictionMatch() throws Exception {
        mvc.perform(get("/api/v1/model/info")).andExpect(status().isOk())
            .andExpect(jsonPath("$.fields.length()").value(5))
            .andExpect(jsonPath("$.fields[0].key").value("age"))
            .andExpect(jsonPath("$.fields[0].min").doesNotExist())
            .andExpect(jsonPath("$.modelVersion").value("demo-example"))
            .andExpect(jsonPath("$.predictionType").value("estimated_current_measurement"))
            .andExpect(jsonPath("$.demo").value(true));
        mvc.perform(post("/api/v1/predictions").contentType("application/json").content(VALID))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.predictedValue").value(134.2))
            .andExpect(jsonPath("$.unit").value("мм рт. ст."))
            .andExpect(jsonPath("$.modelVersion").value("demo-example"))
            .andExpect(jsonPath("$.predictionType").value("estimated_current_measurement"))
            .andExpect(jsonPath("$.demo").value(true));
        mvc.perform(get("/api/v1/health/ready")).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DEMO"))
            .andExpect(jsonPath("$.demo").value(true));
    }
    @Test void missingAndWrongTypesAreFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/predictions").contentType("application/json")
            .content("{\"age\":\"52\",\"sex\":\"other\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fields.length()").value(5))
            .andExpect(jsonPath("$.fields[*].field").value(org.hamcrest.Matchers.containsInAnyOrder(
                    "age", "sex", "bmi", "totalCholesterol", "glucose")));
    }
    @Test void malformedAndUnknownFieldsAreGeneralErrors() throws Exception {
        for (String body : new String[]{"[]", "null", "{", "{} {}", "{\"age\":1,\"age\":2}",
                "{\"password\":\"sensitive\"}", "{\"age\":NaN}"}) {
            mvc.perform(post("/api/v1/predictions").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        }
    }
    @Test void nullAndOverflowAreRejected() throws Exception {
        for (String age : new String[]{"null", "1e999"}) {
            mvc.perform(post("/api/v1/predictions").contentType("application/json")
                .content(VALID.replace("52", age))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[0].field").value("age"));
        }
    }
    @Test void corsAllowsFrontendAndRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/v1/predictions").header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/v1/predictions").header("Origin", "https://unknown.example")
            .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }
}
