package kz.medpredict.prediction.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void documentsFlatDynamicPayloadAndLocalizedErrorsWithoutRequiringAvailableModel() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("MedPredict API"))
                .andExpect(jsonPath("$.paths['/api/v1/model/info'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.parameters[*].name", hasItem("Accept-Language")))
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.requestBody.content['application/json'].schema['$ref']")
                        .value("#/components/schemas/PredictionCreateRequest"))
                .andExpect(jsonPath("$.components.schemas.PredictionCreateRequest.type").value("object"))
                .andExpect(jsonPath("$.components.schemas.PredictionCreateRequest.properties.values").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.PredictionCreateRequest.additionalProperties.oneOf.length()").value(2))
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.responses['400'].content['application/json'].schema.oneOf.length()").value(2))
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.responses['400'].content['application/json'].examples['Kazakh (kk)'].value.fields[0].message")
                        .value("Бұл өріс міндетті."))
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.responses['503']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/predictions'].post.responses['500']").exists())
                .andExpect(jsonPath("$.components.schemas.PredictionResultResponse.properties.predictedValue").exists())
                .andExpect(jsonPath("$.components.schemas.PredictionModelInfoResponse.properties.fields").exists());
    }

    @Test
    void swaggerUiAndItsConfigurationAreServed() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
