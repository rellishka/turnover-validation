package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.TurnoverStatus;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.importRunEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.leaseEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.propertyEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.tenantEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.turnoverEntity;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract test for the {@code analytics.v1_monthly_turnover} view: it guards the
 * exposed output columns (selecting each by name fails if one is renamed/removed)
 * and the rule that only validated (ACCEPTED / CORRECTED) turnover leaves the view.
 */
@DataJpaTest
class MonthlyTurnoverViewContractTest {

    @Autowired
    private TestEntityManager entityManager;

    private PropertyEntity property;
    private TenantEntity tenant;
    private LeaseEntity lease;
    private ImportRunEntity importRun;

    @BeforeEach
    void setUp() {
        property = entityManager.persistFlushFind(propertyEntity());
        tenant = entityManager.persistFlushFind(tenantEntity());
        lease = entityManager.persistFlushFind(leaseEntity(property, tenant));
        importRun = entityManager.persistFlushFind(importRunEntity());
    }

    @Test
    void exposesContractColumnsForValidatedTurnoverOnly() {
        entityManager.persist(turnoverEntity(lease, importRun, TurnoverStatus.ACCEPTED));
        entityManager.persist(turnoverEntity(lease, importRun, TurnoverStatus.CORRECTED));
        entityManager.persist(turnoverEntity(lease, importRun, TurnoverStatus.FLAGGED));
        entityManager.persist(turnoverEntity(lease, importRun, TurnoverStatus.RECEIVED));
        entityManager.flush();

        // Selecting each column by name is itself the contract guard: a renamed or
        // removed output column makes this query fail.
        Query query = entityManager.getEntityManager().createNativeQuery("""
                SELECT tenant_external_id, tenant_name,
                       property_external_id, property_name, property_country,
                       period, amount, currency, status
                FROM analytics.v1_monthly_turnover
                ORDER BY status""");

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        // Only ACCEPTED + CORRECTED leave the view; FLAGGED / RECEIVED are filtered out.
        assertEquals(2, rows.size());
        assertTrue(rows.stream().map(row -> row[8]).noneMatch(status ->
                status.equals(TurnoverStatus.FLAGGED.name()) || status.equals(TurnoverStatus.RECEIVED.name())));

        // ACCEPTED sorts before CORRECTED; check the denormalized columns against the fixtures.
        Object[] accepted = rows.getFirst();
        assertEquals(tenant.getExternalId(), accepted[0]);
        assertEquals(tenant.getName(), accepted[1]);
        assertEquals(property.getExternalId(), accepted[2]);
        assertEquals(property.getName(), accepted[3]);
        assertEquals(property.getCountry(), accepted[4]);
        assertEquals(PERIOD.toString(), accepted[5]);
        assertEquals(0, AMOUNT.compareTo((BigDecimal) accepted[6]));
        assertEquals(CURRENCY, accepted[7]);
        assertEquals(TurnoverStatus.ACCEPTED.name(), accepted[8]);
    }
}
