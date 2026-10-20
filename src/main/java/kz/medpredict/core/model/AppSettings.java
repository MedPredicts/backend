package kz.medpredict.core.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(prefix = "application")
public class AppSettings {
    public static final List<String> SUPPORTED_LANGUAGES = List.of("ru", "en", "kk");

    @NotBlank
    @Pattern(regexp = "ru|en|kk")
    private final String defaultLocale;

    public AppSettings() {
        this("ru");
    }

    @ConstructorBinding
    public AppSettings(@DefaultValue("ru") String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getLocale() {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return SUPPORTED_LANGUAGES.contains(language) ? language : defaultLocale;
    }
}
