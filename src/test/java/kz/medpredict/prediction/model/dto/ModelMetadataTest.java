package kz.medpredict.prediction.model.dto;

import java.util.ArrayList;
import java.util.List;
import kz.medpredict.core.exception.dto.FieldError;
import kz.medpredict.core.exception.dto.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelMetadataTest {
    @Test
    void metadataDoesNotChangeWhenSourceCollectionsChange() {
        var options = new ArrayList<>(List.of(new Option("a", "A")));
        var field = new Field("category", "Category", "select", true,
                null, null, null, null, options);
        var fields = new ArrayList<>(List.of(field));
        var metadata = new ModelInfo("Target", "units", 1, "v1", fields);

        options.clear();
        fields.clear();

        assertThat(metadata.fields()).containsExactly(field);
        assertThat(field.options()).containsExactly(new Option("a", "A"));
        assertThatThrownBy(() -> metadata.fields().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> field.options().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void numberFieldsRetainAbsentOptions() {
        var field = new Field("value", "Value", "number", true,
                "units", null, null, null, null);
        assertThat(field.options()).isNull();
    }

    @Test
    void validationErrorsDoNotChangeAfterConstruction() {
        var error = new FieldError("value", "Required");
        var fields = new ArrayList<>(List.of(error));
        var response = new ValidationErrorResponse("VALIDATION_ERROR", fields);
        fields.clear();
        assertThat(response.fields()).containsExactly(error);
        assertThatThrownBy(() -> response.fields().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
