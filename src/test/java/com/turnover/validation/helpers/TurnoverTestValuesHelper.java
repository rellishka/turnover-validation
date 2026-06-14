package com.turnover.validation.helpers;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.domain.ImportRunStatus;
import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.Property;
import com.turnover.validation.application.domain.Tenant;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.TurnoverSubmission;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

public final class TurnoverTestValuesHelper {

    private TurnoverTestValuesHelper() {
    }

    public static final YearMonth PERIOD = YearMonth.of(2026, 5);
    public static final BigDecimal AMOUNT = new BigDecimal("100000");
    public static final String CURRENCY = "EUR";
    public static final Instant SUBMITTED_AT =
            LocalDateTime.of(2026, 6, 3, 10, 0).toInstant(ZoneOffset.UTC);
    public static final String TENANT_EXTERNAL_ID = "T-1042";
    public static final String PROPERTY_EXTERNAL_ID = "P-007";
    public static final Long TURNOVER_ID = 1L;
    public static final Long IMPORT_RUN_ID = 1L;
    public static final int ENTRIES_IMPORTED = 42;

    public static Property property() {
        return new Property(1L, PROPERTY_EXTERNAL_ID, "Kastanjelaan", "Netherlands", "Amsterdam");
    }

    public static Tenant tenant() {
        return new Tenant(1L, TENANT_EXTERNAL_ID, "Zalando");
    }

    public static Lease lease() {
        return new Lease(1L, property(), tenant(), LocalDate.of(2024, 1, 1), null);
    }

    public static ImportRun importRun() {
        return new ImportRun(PERIOD);
    }

    public static ImportRun succeededImportRun() {
        return new ImportRun(
                IMPORT_RUN_ID, SUBMITTED_AT, SUBMITTED_AT, ImportRunStatus.SUCCESS, PERIOD, ENTRIES_IMPORTED, null);
    }

    public static Turnover turnover(TurnoverStatus status) {
        return turnover(AMOUNT, status);
    }

    public static Turnover turnover(BigDecimal amount, TurnoverStatus status) {
        return new Turnover(TURNOVER_ID, lease(), importRun(), PERIOD, amount, CURRENCY, status, SUBMITTED_AT);
    }

    public static TurnoverSubmission submission() {
        return submission(AMOUNT);
    }

    public static TurnoverSubmission submission(BigDecimal amount) {
        return new TurnoverSubmission(TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID, PERIOD, amount, CURRENCY, SUBMITTED_AT);
    }
}
