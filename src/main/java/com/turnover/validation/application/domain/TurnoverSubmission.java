package com.turnover.validation.application.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

/**
 * A raw turnover record as fetched from the external Tenant App, keyed by the
 * Tenant App's external identifiers. The import flow resolves these to a
 * {@link Lease} and builds a {@link Turnover}.
 */
public record TurnoverSubmission(
        String tenantExternalId,
        String propertyExternalId,
        YearMonth period,
        BigDecimal amount,
        String currency,
        Instant submittedAt
) {
}
