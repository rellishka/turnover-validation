package com.turnover.validation.application.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

/**
 * A generic success-or-error result wrapper used at the manager/service boundary:
 * it holds either the resulting object (success) or a message plus a
 * {@link ValidationErrorCode} (failure), which the application service maps to an
 * exception. Think of it as {@code Result<T>} / {@code Either}.
 *
 * <p><strong>Note: this is NOT the domain validation.</strong> Domain
 * well-formedness lives in {@link Validatable#isValid()} on the domain records
 * (e.g. {@link Turnover}). {@code Validation} only carries the <em>outcome</em> of
 * an operation, not the rules themselves.
 *
 * @param <T> the type of the result object on success
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
