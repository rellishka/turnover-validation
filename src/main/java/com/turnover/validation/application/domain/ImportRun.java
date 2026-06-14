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

    /** A new run for the given period, marked RUNNING. */
    public ImportRun(YearMonth period) {
        this(null, Instant.now(), null, ImportRunStatus.RUNNING, period, 0, null);
    }

    /** This run finished successfully, importing the given number of entries. */
    public ImportRun succeeded(int entriesImported) {
        return new ImportRun(id, startedAt, Instant.now(), ImportRunStatus.SUCCESS, periodFetched, entriesImported, null);
    }

    /** This run failed with the given error message. */
    public ImportRun failed(String errorMessage) {
        return new ImportRun(id, startedAt, Instant.now(), ImportRunStatus.FAILED, periodFetched, 0, errorMessage);
    }
}
