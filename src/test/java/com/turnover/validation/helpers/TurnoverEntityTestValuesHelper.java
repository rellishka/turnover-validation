package com.turnover.validation.helpers;

import com.turnover.validation.adapter.out.persistence.ImportRunEntity;
import com.turnover.validation.adapter.out.persistence.LeaseEntity;
import com.turnover.validation.adapter.out.persistence.PropertyEntity;
import com.turnover.validation.adapter.out.persistence.TenantEntity;
import com.turnover.validation.adapter.out.persistence.TurnoverEntity;
import com.turnover.validation.application.domain.ImportRunStatus;
import com.turnover.validation.application.domain.TurnoverStatus;

import java.time.LocalDate;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PROPERTY_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.SUBMITTED_AT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TENANT_EXTERNAL_ID;

public final class TurnoverEntityTestValuesHelper {

    private TurnoverEntityTestValuesHelper() {
    }

    public static PropertyEntity propertyEntity() {
        return new PropertyEntity(null, PROPERTY_EXTERNAL_ID, "Kastanjelaan", "Netherlands", "Amsterdam");
    }

    public static TenantEntity tenantEntity() {
        return new TenantEntity(null, TENANT_EXTERNAL_ID, "Zalando");
    }

    public static LeaseEntity leaseEntity(PropertyEntity property, TenantEntity tenant) {
        return new LeaseEntity(null, property, tenant, LocalDate.of(2024, 1, 1), null);
    }

    public static ImportRunEntity importRunEntity() {
        return new ImportRunEntity(null, SUBMITTED_AT, SUBMITTED_AT, ImportRunStatus.SUCCESS, PERIOD, 1, null);
    }

    public static TurnoverEntity turnoverEntity(LeaseEntity lease, ImportRunEntity importRun, TurnoverStatus status) {
        return new TurnoverEntity(null, lease, importRun, PERIOD, AMOUNT, CURRENCY, status, SUBMITTED_AT);
    }
}
