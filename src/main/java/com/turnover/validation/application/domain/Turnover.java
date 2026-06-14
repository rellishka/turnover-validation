package com.turnover.validation.application.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

/**
 * A turnover figure reported by a tenant for one lease and one month.
 *
 * <ul>
 *   <li>{@code period} — the reporting month the figure is <em>for</em> (e.g. 2026-05).</li>
 *   <li>{@code submittedAt} — when the tenant <em>entered</em> the figure in the Tenant App
 *       (source-system provenance, carried through on import). It is distinct from when we
 *       ingested it (see {@link ImportRun}). Used for audit and to surface reporting lag /
 *       late submissions; it is not part of any validation rule.</li>
 * </ul>
 */
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
