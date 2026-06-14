package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.ValidationIssue;
import com.turnover.validation.application.domain.ValidationIssueStatus;
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
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.turnoverEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ValidationIssuePersistenceAdapter.class, ValidationIssuePersistenceMapper.class,
        TurnoverPersistenceMapper.class, LeasePersistenceMapper.class, ImportRunPersistenceMapper.class})
class ValidationIssuePersistenceAdapterTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ValidationIssuePersistenceAdapter adapter;

    private Long turnoverId;

    @BeforeEach
    void setUp() {
        PropertyEntity property = entityManager.persistFlushFind(propertyEntity());
        TenantEntity tenant = entityManager.persistFlushFind(tenantEntity());
        LeaseEntity lease = entityManager.persistFlushFind(leaseEntity(property, tenant));
        ImportRunEntity importRun = entityManager.persistFlushFind(importRunEntity());
        turnoverId = entityManager.persistFlushFind(turnoverEntity(lease, importRun, TurnoverStatus.FLAGGED)).getId();
    }

    @Test
    void savePersistsIssueAndFindOpenReturnsIt() {
        Turnover turnoverRef = turnoverRef();
        ValidationIssue issue = new ValidationIssue(
                null, turnoverRef, "MONTH_OVER_MONTH_DEVIATION", "deviation",
                ValidationIssueStatus.OPEN, null, null, null);

        ValidationIssue saved = adapter.save(issue);
        assertNotNull(saved.id());

        Optional<ValidationIssue> open = adapter.findOpenByTurnover(turnoverRef);

        assertTrue(open.isPresent());
        assertEquals(ValidationIssueStatus.OPEN, open.orElseThrow().status());
    }

    @Test
    void findOpenByTurnoverReturnsEmptyWhenNoOpenIssue() {
        assertTrue(adapter.findOpenByTurnover(turnoverRef()).isEmpty());
    }

    private Turnover turnoverRef() {
        return new Turnover(turnoverId, null, null, null, null, null, null, null);
    }
}
