package com.turnover.validation.adapter.out.tenantapi;

import java.math.BigDecimal;
import java.time.Instant;

/** A single turnover record as returned by the Tenant App API (wire format). */
public record TenantApiTurnover(
        String tenantId,
        String propertyId,
        String period,
        BigDecimal turnover,
        String currency,
        Instant submittedAt
) {
}
