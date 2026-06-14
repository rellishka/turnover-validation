package com.turnover.validation.application.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

/**
 * This class represents a validation that either has an object or an error.
 * In the case it has an object it means the validation was successful.
 * In case it has an error it means the validation failed.
 *
 * @param <T> the validated object type
 */
@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Validation<T> {

    private final T object;
    private final String message;
    private final ValidationErrorCode validationErrorCode;

    public static <T> Validation<T> success(T object) {
        return new Validation<>(object, null, null);
    }

    public static <T> Validation<T> error(String message) {
        return new Validation<>(null, message, ValidationErrorCode.GENERIC);
    }

    public static <T> Validation<T> error(String message, ValidationErrorCode validationErrorCode) {
        return new Validation<>(null, message, validationErrorCode);
    }

    public boolean isValid() {
        return object != null;
    }

    public Optional<T> getObject() {
        return Optional.ofNullable(object);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
