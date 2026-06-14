package com.turnover.validation.helpers;

import com.turnover.validation.adapter.out.tenantapi.TenantApiPage;
import com.turnover.validation.adapter.out.tenantapi.TenantApiTurnover;

import java.util.List;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PROPERTY_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.SUBMITTED_AT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TENANT_EXTERNAL_ID;

public final class TenantApiTestValuesHelper {

    private TenantApiTestValuesHelper() {
    }

    public static TenantApiTurnover tenantApiTurnover() {
        return tenantApiTurnover(TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID);
    }

    public static TenantApiTurnover tenantApiTurnover(String tenantExternalId, String propertyExternalId) {
        return new TenantApiTurnover(tenantExternalId, propertyExternalId, PERIOD.toString(), AMOUNT, CURRENCY, SUBMITTED_AT);
    }

    public static TenantApiPage tenantApiPage(int page, int totalPages, List<TenantApiTurnover> items) {
        return new TenantApiPage(page, totalPages, items);
    }
}
