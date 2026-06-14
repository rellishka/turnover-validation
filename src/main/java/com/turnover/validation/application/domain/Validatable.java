package com.turnover.validation.application.domain;

/**
 * Domain well-formedness check, implemented by domain records (e.g. {@link Turnover}).
 * This is the actual domain validation — distinct from {@link Validation}, which is
 * just a generic success-or-error result wrapper.
 */
public interface Validatable {
    boolean isValid();
}
