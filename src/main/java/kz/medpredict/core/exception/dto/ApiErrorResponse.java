package kz.medpredict.core.exception.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public final class ApiErrorResponse {
    @NotBlank
    private final String error;
    @NotBlank
    private final String message;

}
