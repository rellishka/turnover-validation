package com.turnover.validation.application.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationTest {

    @Test
    void successHoldsTheObjectAndNoError() {
        String value = "ok";

        Validation<String> validation = Validation.success(value);

        assertTrue(validation.isValid());
        assertSame(value, validation.getObject().orElseThrow());
        assertTrue(validation.getMessage().isEmpty());
        assertNull(validation.getValidationErrorCode());
    }

    @Test
    void errorHoldsMessageAndDefaultsToGenericCode() {
        String message = "boom";

        Validation<String> validation = Validation.error(message);

        assertFalse(validation.isValid());
        assertTrue(validation.getObject().isEmpty());
        assertEquals(message, validation.getMessage().orElseThrow());
        assertEquals(ValidationErrorCode.GENERIC, validation.getValidationErrorCode());
    }

    @Test
    void errorWithCodeHoldsThatCode() {
        Validation<String> validation = Validation.error("nope", ValidationErrorCode.INVALID_TURNOVER);

        assertEquals(ValidationErrorCode.INVALID_TURNOVER, validation.getValidationErrorCode());
    }
}
