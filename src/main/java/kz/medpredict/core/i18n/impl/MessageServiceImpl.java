package kz.medpredict.core.i18n.impl;

import java.util.Locale;
import kz.medpredict.core.i18n.MessageService;
import kz.medpredict.core.model.AppSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageSource messageSource;
    private final AppSettings appSettings;

    @Override
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, Locale.forLanguageTag(appSettings.getLocale()));
    }

    @Override
    public String getMessage(String code) {
        return getMessage(code, new Object[0]);
    }
}
