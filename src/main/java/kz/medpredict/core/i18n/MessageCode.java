package kz.medpredict.core.i18n;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageCode {
    public static final String SYSTEM_ERROR = "error.system.unexpected";
    public static final String VALIDATION_FAILED = "validation.input.invalid";
    public static final String INVALID_REQUEST = "error.request.invalid";
    public static final String MODEL_UNAVAILABLE = "error.model.unavailable";
    public static final String MODEL_INCOMPATIBLE = "error.model.incompatible";
    public static final String MODEL_INVALID_RESPONSE = "error.model.invalid-response";
    public static final String FIELD_REQUIRED = "validation.field.required";
    public static final String NUMBER_REQUIRED = "validation.number.required";
    public static final String NUMBER_CONSTRAINTS = "validation.number.constraints";
    public static final String OPTION_INVALID = "validation.option.invalid";
}
