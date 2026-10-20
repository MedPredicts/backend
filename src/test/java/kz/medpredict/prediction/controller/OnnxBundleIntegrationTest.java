package kz.medpredict.prediction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import kz.medpredict.prediction.infrastructure.model.ModelArtifactMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Optional: execute against the real exported artifact, never against a demo provider. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("onnx")
@EnabledIfSystemProperty(named = "medpredict.model.test-directory", matches = ".+")
class OnnxBundleIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @DynamicPropertySource
    static void configureBundle(DynamicPropertyRegistry registry) {
        registry.add("medpredict.model.directory", () -> System.getProperty("medpredict.model.test-directory"));
    }

    @Test
    void startsWithRealArtifactAndMatchesPythonGoldenPredictions() throws Exception {
        var metadata = json("metadata.json");
        var golden = json("golden.json");
        assertThat(golden.path("featureOrder").toString()).isEqualTo(mapper.valueToTree(ModelArtifactMetadata.FEATURE_ORDER).toString());
        double tolerance = golden.path("absoluteTolerance").doubleValue();
        assertThat(tolerance).isGreaterThan(0).isLessThanOrEqualTo(0.01);
        assertThat(golden.path("cases").isArray()).isTrue();
        assertThat(golden.path("cases").size()).isGreaterThanOrEqualTo(2);
        mvc.perform(get("/api/v1/health/ready")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true)).andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.demo").value(false))
                .andExpect(jsonPath("$.modelVersion").value(metadata.path("modelVersion").textValue()));
        mvc.perform(get("/api/v1/model/info")).andExpect(status().isOk())
                .andExpect(jsonPath("$.fields.length()").value(5))
                .andExpect(jsonPath("$.fields[4].unit").value("ммоль/л"))
                .andExpect(jsonPath("$.predictionType").value("estimated_current_measurement"))
                .andExpect(jsonPath("$.demo").value(false));
        var distinctOutputs = new HashSet<Double>();
        double maximumError = 0;
        for (var example : golden.path("cases")) {
            double expected = example.path("expected").doubleValue();
            assertThat(example.path("expected").isNumber() && Double.isFinite(expected)).isTrue();
            String response = mvc.perform(post("/api/v1/predictions").contentType("application/json")
                            .content(mapper.writeValueAsBytes(example.path("input"))))
                    .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(jsonPath("$.demo").value(false))
                    .andExpect(jsonPath("$.predictionType").value("estimated_current_measurement"))
                    .andExpect(jsonPath("$.modelVersion").value(metadata.path("modelVersion").textValue()))
                    .andExpect(jsonPath("$.unit").value(metadata.path("unit").textValue()))
                    .andReturn().getResponse().getContentAsString();
            double actual = mapper.readTree(response).path("predictedValue").doubleValue();
            assertThat(Math.abs(actual - expected)).isLessThanOrEqualTo(tolerance);
            maximumError = Math.max(maximumError, Math.abs(actual - expected));
            distinctOutputs.add(actual);
        }
        assertThat(distinctOutputs.size()).isGreaterThan(1);
        var report = mapper.createObjectNode().put("modelVersion", metadata.path("modelVersion").textValue())
                .put("modelSha256", metadata.path("sha256").textValue())
                .put("javaOnnxRuntimeVersion", ai.onnxruntime.OrtEnvironment.getEnvironment().getVersion())
                .put("caseCount", golden.path("cases").size()).put("absoluteTolerance", tolerance)
                .put("maximumAbsoluteError", maximumError).put("passed", true);
        Files.writeString(Path.of("target", "model-parity-report.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    }

    @Test
    void rejectsInputsOutsideTheArtifactDomainBeforeInference() throws Exception {
        var metadata = json("metadata.json");
        var input = json("golden.json").path("cases").get(0).path("input").deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) input).put("age", metadata.path("fields").get(0).path("min").doubleValue() - 1);
        mvc.perform(post("/api/v1/predictions").contentType("application/json").content(mapper.writeValueAsBytes(input)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields[0].field").value("age"))
                .andExpect(jsonPath("$.predictedValue").doesNotExist());
    }

    private JsonNode json(String name) throws Exception {
        return mapper.readTree(Files.readAllBytes(Path.of(System.getProperty("medpredict.model.test-directory")).resolve(name)));
    }
}
