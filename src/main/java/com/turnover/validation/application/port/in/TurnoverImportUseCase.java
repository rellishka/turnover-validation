package com.turnover.validation.application.port.in;

import com.turnover.validation.application.domain.ImportRun;

import java.time.YearMonth;

/**
 * Inbound use case for ingesting tenant turnover from the external Tenant App.
 *
 * <p>Driven by the monthly schedule or a manual trigger. The implementation
 * fetches the period's submissions, maps each to a turnover, runs the validation
 * rules (flagging significant deviations for review), and persists the result.
 */
public interface TurnoverImportUseCase {

    /**
     * Import all turnover submitted for the given reporting month.
     *
     * <p>The operation is idempotent: re-running the same period does not create
     * duplicate turnover (one turnover per lease per period). Every run is
     * recorded as an {@link ImportRun} audit entry — successful runs carry the
     * number of imported entries, failed runs carry the error message — so a
     * failed import is observable and can be safely re-triggered.
     *
     * @param period the reporting month to import (e.g. {@code 2026-05})
     * @return the {@link ImportRun} audit record describing the outcome
     *         ({@code SUCCESS} with an entry count, or {@code FAILED} with an error message)
     */
    ImportRun importTurnoverForPeriod(YearMonth period);
}
