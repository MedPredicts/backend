package kz.medpredict.core.exception.handler;

import java.util.Locale;
import kz.medpredict.core.exception.ServiceValidationException;
import kz.medpredict.core.i18n.impl.MessageServiceImpl;
import kz.medpredict.core.model.AppSettings;
import kz.medpredict.core.util.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void serviceErrorsUseEnumStatusAndLocalizedArguments() {
        var messages = new StaticMessageSource();
        messages.addMessage("test.limit", Locale.ENGLISH, "Allowed maximum: {0}");
        var handler = new GlobalExceptionHandler(new MessageServiceImpl(messages, new AppSettings()));
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        for (ErrorCode code : ErrorCode.values()) {
            var response = handler.serviceValidation(new ServiceValidationException(code, "test.limit", 10));
            assertThat(response.getStatusCode()).isEqualTo(code.getStatus());
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo(code.name());
            assertThat(response.getBody().getMessage()).isEqualTo("Allowed maximum: 10");
        }
    }

    @Test
    void exceptionArgumentsCannotBeMutatedThroughSourceOrGetter() {
        Object[] arguments = {10};
        var exception = new ServiceValidationException(ErrorCode.INVALID_REQUEST, "test.limit", arguments);
        arguments[0] = 20;
        exception.getArguments()[0] = 30;
        assertThat(exception.getArguments()).containsExactly(10);
        assertThat(exception.getMessage()).isEqualTo("test.limit");
    }
}
