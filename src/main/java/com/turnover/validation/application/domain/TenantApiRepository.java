package com.turnover.validation.application.domain;

import java.time.YearMonth;
import java.util.List;

/**
 * Domain-facing contract for fetching turnover submissions from the external
 * Tenant App. The outbound port {@code TenantApiPort} extends this, and an
 * adapter in adapter.out implements it.
 */
public interface TenantApiRepository {

    List<TurnoverSubmission> fetchTurnover(YearMonth period);
}
