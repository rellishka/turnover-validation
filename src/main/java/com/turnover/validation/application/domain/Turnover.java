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

    /** Significant deviation detected — needs Asset Manager review. */
    public Turnover flag() {
        return withStatus(TurnoverStatus.FLAGGED);
    }

    /** Confirmed correct as reported. */
    public Turnover accept() {
        return withStatus(TurnoverStatus.ACCEPTED);
    }

    /** Corrected to the agreed figure after contacting the tenant. */
    public Turnover correct(BigDecimal correctedAmount) {
        return new Turnover(id, lease, importRun, period, correctedAmount, currency, TurnoverStatus.CORRECTED, submittedAt);
    }

    private Turnover withStatus(TurnoverStatus newStatus) {
        return new Turnover(id, lease, importRun, period, amount, currency, newStatus, submittedAt);
    }
}
