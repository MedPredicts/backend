package kz.medpredict.core.configuration;

import java.util.Locale;
import kz.medpredict.core.model.AppSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({CorsProperties.class, AppSettings.class})
public class WebConfiguration implements WebMvcConfigurer {
    private final CorsProperties corsProperties;
    private final AppSettings appSettings;

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(AppSettings.SUPPORTED_LANGUAGES.stream()
                .map(Locale::forLanguageTag).toList());
        resolver.setDefaultLocale(Locale.forLanguageTag(appSettings.getDefaultLocale()));
        return resolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Accept", "Content-Type", "Accept-Language")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NoStoreInterceptor()).addPathPatterns("/api/**");
    }
}
