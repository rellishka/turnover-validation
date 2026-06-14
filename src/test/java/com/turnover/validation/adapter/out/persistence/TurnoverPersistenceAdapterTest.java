package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.importRunEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.leaseEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.propertyEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.tenantEntity;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PROPERTY_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.SUBMITTED_AT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TENANT_EXTERNAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({TurnoverPersistenceAdapter.class, TurnoverPersistenceMapper.class,
        LeasePersistenceMapper.class, ImportRunPersistenceMapper.class})
class TurnoverPersistenceAdapterTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TurnoverPersistenceAdapter adapter;

    private Long leaseId;
    private Long importRunId;

    @BeforeEach
    void setUp() {
        PropertyEntity property = entityManager.persistFlushFind(propertyEntity());
        TenantEntity tenant = entityManager.persistFlushFind(tenantEntity());
        leaseId = entityManager.persistFlushFind(leaseEntity(property, tenant)).getId();
        importRunId = entityManager.persistFlushFind(importRunEntity()).getId();
    }

    @Test
    void savePersistsTurnoverAndMapsBackTheFullGraph() {
        Turnover saved = adapter.save(received());

        assertNotNull(saved.id());
        assertEquals(AMOUNT, saved.amount());
        assertEquals(TurnoverStatus.RECEIVED, saved.status());
        assertEquals(PROPERTY_EXTERNAL_ID, saved.lease().property().externalId());
        assertEquals(TENANT_EXTERNAL_ID, saved.lease().tenant().externalId());
    }

    @Test
    void findByIdReturnsTheSavedTurnover() {
        Turnover saved = adapter.save(received());

        Optional<Turnover> found = adapter.findById(saved.id());

        assertTrue(found.isPresent());
        assertEquals(saved.id(), found.orElseThrow().id());
    }

    @Test
    void findByLeaseAndPeriodReturnsMatch() {
        adapter.save(received());

        Optional<Turnover> found = adapter.findByLeaseAndPeriod(leaseRef(), PERIOD);

        assertTrue(found.isPresent());
    }

    @Test
    void findByStatusReturnsOnlyMatchingTurnover() {
        adapter.save(received());

        assertEquals(1, adapter.findByStatus(TurnoverStatus.RECEIVED).size());
        assertTrue(adapter.findByStatus(TurnoverStatus.FLAGGED).isEmpty());
    }

    private Turnover received() {
        return new Turnover(null, leaseRef(), importRunRef(), PERIOD, AMOUNT, CURRENCY, TurnoverStatus.RECEIVED, SUBMITTED_AT);
    }

    private Lease leaseRef() {
        return new Lease(leaseId, null, null, null, null);
    }

    private ImportRun importRunRef() {
        return new ImportRun(importRunId, null, null, null, null, 0, null);
    }
}
