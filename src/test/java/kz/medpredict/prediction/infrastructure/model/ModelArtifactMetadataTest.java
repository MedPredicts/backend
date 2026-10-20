package kz.medpredict.prediction.infrastructure.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
import kz.medpredict.prediction.validation.ModelMetadataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ModelArtifactMetadataTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final jakarta.validation.ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final ModelMetadataValidator validator = new ModelMetadataValidator(factory.getValidator());

    public static final String METADATA = """
            {"schemaVersion":1,"modelVersion":"synthetic-test-v1","sha256":"0000000000000000000000000000000000000000000000000000000000000000",
             "inputName":"features","outputName":"predictedValue","featureOrder":["age","sex","bmi","totalCholesterol","glucose"],
             "sexEncoding":{"male":0,"female":1},"predictionType":"estimated_current_measurement","demo":false,
             "indicatorName":"Оценка систолического давления","unit":"мм рт. ст.","decimalPlaces":1,"disclaimer":"Synthetic integration fixture; not a trained medical model.",
             "fields":[
               {"key":"age","label":"Возраст","type":"number","required":true,"unit":"лет","min":18,"max":79,"integer":true},
               {"key":"sex","label":"Пол","type":"select","required":true,"options":[{"value":"male","label":"Мужской"},{"value":"female","label":"Женский"}]},
               {"key":"bmi","label":"ИМТ","type":"number","required":true,"unit":"кг/м²","min":10,"max":60},
               {"key":"totalCholesterol","label":"Холестерин","type":"number","required":true,"unit":"ммоль/л","min":1,"max":12},
               {"key":"glucose","label":"Глюкоза натощак","type":"number","required":true,"unit":"ммоль/л","min":2,"max":20}]}
            """;

    @AfterEach
    void closeFactory() {
        factory.close();
    }

    @Test
    void readsVersionedSchemaAndPreservesArtifactDomain() throws Exception {
        var artifact = read(METADATA, "synthetic-test-v1");
        assertThat(artifact.info().getFields()).extracting(field -> field.getKey())
                .containsExactlyElementsOf(ModelArtifactMetadata.FEATURE_ORDER);
        assertThat(artifact.info().getFields().get(0).getMin()).isEqualTo(18);
        assertThat(artifact.info().getDemo()).isFalse();
        assertThat(artifact.info().getPredictionType()).isEqualTo("estimated_current_measurement");
    }

    @Test
    void rejectsWrongSchemaVersionAndUnpinnedVersion() {
        assertThatThrownBy(() -> read(METADATA.replace("\"schemaVersion\":1", "\"schemaVersion\":2"), ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> read(METADATA, "another-version")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDifferentUnitsEncodingFeatureOrderAndNonFiniteBounds() throws Exception {
        for (String invalid : new String[]{
                METADATA.replace("ммоль/л", "mg/dL"),
                METADATA.replace("\"male\":0", "\"male\":1"),
                METADATA.replace("\"featureOrder\":[\"age\",\"sex\"", "\"featureOrder\":[\"sex\",\"age\""),
                METADATA.replace("\"min\":18", "\"min\":1e999"),
                METADATA.replace("\"demo\":false", "\"demo\":true"),
                METADATA.replace("\"integer\":true", "\"integer\":false"),
                METADATA.replace("\"inputName\":\"features\"", "\"inputName\":\"wrong\"")}) {
            assertThatThrownBy(() -> read(invalid, "")).isInstanceOf(IllegalArgumentException.class);
        }
        ObjectNode incomplete = (ObjectNode) mapper.readTree(METADATA);
        incomplete.remove("featureOrder");
        assertThatThrownBy(() -> ModelArtifactMetadata.read(incomplete, validator, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ModelArtifactMetadata.read(null, validator, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ModelArtifactMetadata read(String value, String version) throws Exception {
        return ModelArtifactMetadata.read(mapper.readTree(value), validator, version);
    }
}
