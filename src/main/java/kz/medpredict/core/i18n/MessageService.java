package kz.medpredict.core.i18n;

public interface MessageService {
    String getMessage(String code, Object... args);
    String getMessage(String code);
}
