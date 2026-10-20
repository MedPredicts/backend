package kz.medpredict.core.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI medPredictOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("MedPredict API")
                .version("v1")
                .description("Educational prediction API. Load model metadata before submitting input. "
                        + "The default provider is unavailable (503) until a trained model is connected. "
                        + "The explicit demo profile returns a labeled fixed example, not trained inference. "
                        + "Errors support ru, en and kk via Accept-Language; missing or unsupported languages "
                        + "use the configured default (ru unless overridden). Error codes and field names are stable."));
    }
}
