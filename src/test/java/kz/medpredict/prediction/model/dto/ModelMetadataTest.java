package kz.medpredict.prediction.model.dto;

import java.util.ArrayList;
import java.util.List;
import kz.medpredict.core.exception.dto.FieldValidationErrorResponse;
import kz.medpredict.core.exception.dto.ApiValidationErrorResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelMetadataTest {
    @Test
    void metadataDoesNotChangeWhenSourceCollectionsChange() {
        var options = new ArrayList<>(List.of(new PredictionFieldOptionResponse("a", "A")));
        var field = new PredictionFieldResponse("category", "Category", "select", true,
                null, null, null, null, options);
        var fields = new ArrayList<>(List.of(field));
        var metadata = new PredictionModelInfoResponse("Target", "units", 1, "v1", fields);

        options.clear();
        fields.clear();

        assertThat(metadata.getFields()).containsExactly(field);
        assertThat(field.getOptions()).containsExactly(new PredictionFieldOptionResponse("a", "A"));
        assertThatThrownBy(() -> metadata.getFields().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> field.getOptions().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void numberFieldsRetainAbsentOptions() {
        var field = new PredictionFieldResponse("value", "Value", "number", true,
                "units", null, null, null, null);
        assertThat(field.getOptions()).isNull();
    }

    @Test
    void validationErrorsDoNotChangeAfterConstruction() {
        var error = new FieldValidationErrorResponse("value", "Required");
        var fields = new ArrayList<>(List.of(error));
        var response = new ApiValidationErrorResponse("VALIDATION_ERROR", fields);
        fields.clear();
        assertThat(response.getFields()).containsExactly(error);
        assertThatThrownBy(() -> response.getFields().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
