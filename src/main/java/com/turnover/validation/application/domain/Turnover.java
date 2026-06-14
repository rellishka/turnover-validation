package com.turnover.validation.application.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

public record Turnover(
        Long id,
        Lease lease,
        ImportRun importRun,
        YearMonth period,
        BigDecimal amount,
        String currency,
        TurnoverStatus status,
        Instant submittedAt
) implements Validatable {

    @Override
    public boolean isValid() {
        return Objects.nonNull(lease)
                && Objects.nonNull(period)
                && Objects.nonNull(status)
                && Objects.nonNull(amount)
                && amount.signum() >= 0
                && Objects.nonNull(currency)
                && !currency.isBlank();
    }
}
