package kz.medpredict.prediction.infrastructure.documentation;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PredictionOpenApiConfiguration {
    @Bean
    public OpenApiCustomizer predictionCreateRequestSchema() {
        return api -> api.getComponents().addSchemas("PredictionCreateRequest", new ObjectSchema()
                .description("Flat input object. Keys, required fields, categories, units and numeric constraints "
                        + "come from GET /api/v1/model/info. Only keys declared by that metadata are accepted. "
                        + "Values are finite JSON numbers or category strings; null, arrays and objects are invalid. "
                        + "The example applies only to the demo provider, not future models.")
                .additionalProperties(new ComposedSchema()
                        .addOneOfItem(new NumberSchema()).addOneOfItem(new StringSchema())));
    }
}
