package com.turnover.validation.application.domain;

import java.time.Instant;
import java.time.YearMonth;

public record ImportRun(
        Long id,
        Instant startedAt,
        Instant finishedAt,
        ImportRunStatus status,
        YearMonth periodFetched,
        int entriesImported,
        String errorMessage
) {
}
