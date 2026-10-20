package kz.medpredict.core.i18n.impl;

import java.util.Locale;
import kz.medpredict.core.model.AppSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import static org.assertj.core.api.Assertions.assertThat;

class MessageServiceImplTest {
    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void formatsArgumentsAndFallsBackToKeyWhenMissing() {
        var source = new StaticMessageSource();
        source.addMessage("example", Locale.ENGLISH, "Allowed maximum: {0}");
        var service = new MessageServiceImpl(source, new AppSettings());
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(service.getMessage("example", 10)).isEqualTo("Allowed maximum: 10");
        assertThat(service.getMessage("unknown.code")).isEqualTo("unknown.code");
    }

    @Test
    void unsupportedLanguageUsesConfiguredDefault() {
        var settings = new AppSettings("kk");
        var source = new StaticMessageSource();
        source.addMessage("example", Locale.forLanguageTag("kk"), "Мысал");
        LocaleContextHolder.setLocale(Locale.FRENCH);
        assertThat(new MessageServiceImpl(source, settings).getMessage("example")).isEqualTo("Мысал");
    }
}
