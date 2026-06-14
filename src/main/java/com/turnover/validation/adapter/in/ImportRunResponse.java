package com.turnover.validation.adapter.in;

import com.turnover.validation.application.domain.ImportRunStatus;

import java.time.Instant;

/**
 * HTTP response describing the outcome of a manually triggered import.
 *
 * <p>{@code status} is {@code SUCCESS} (with an {@code entriesImported} count) or
 * {@code FAILED} (with an {@code errorMessage}). The reporting month is exposed as
 * an {@code YYYY-MM} string to keep the contract stable.
 */
public record ImportRunResponse(
        Long id,
        ImportRunStatus status,
        String period,
        int entriesImported,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage
) {
}
