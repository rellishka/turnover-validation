package com.turnover.validation.integration;

import com.turnover.validation.adapter.out.persistence.ImportRunEntity;
import com.turnover.validation.adapter.out.persistence.LeaseEntity;
import com.turnover.validation.adapter.out.persistence.PropertyEntity;
import com.turnover.validation.adapter.out.persistence.TenantEntity;
import com.turnover.validation.adapter.out.persistence.TurnoverEntity;
import com.turnover.validation.adapter.out.persistence.ValidationIssueEntity;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.ValidationIssueStatus;
import com.turnover.validation.application.domain.ValidationRule;
import com.turnover.validation.application.port.in.TurnoverReviewUseCase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.importRunEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.leaseEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.propertyEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.tenantEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.turnoverEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end review: correcting a flagged turnover through the use-case port drives
 * the real domain rules + persistence, sets the turnover to CORRECTED, and resolves
 * its open ValidationIssue with the audit trail (resolution / resolvedBy / resolvedAt).
 */
@SpringBootTest
@Transactional
class TurnoverReviewIntegrationTest {

    private static final BigDecimal CORRECTED_AMOUNT = new BigDecimal("75000");
    private static final String REASON = "Verified with tenant; corrected figure";
    private static final String RESOLVED_BY = "a.manager@property.example";

    @Autowired
    private TurnoverReviewUseCase reviewUseCase;
    @Autowired
    private EntityManager entityManager;

    private Long turnoverId;
    private Long issueId;

    @BeforeEach
    void setUp() {
        PropertyEntity property = propertyEntity();
        TenantEntity tenant = tenantEntity();
        entityManager.persist(property);
        entityManager.persist(tenant);

        LeaseEntity lease = leaseEntity(property, tenant);
        entityManager.persist(lease);

        ImportRunEntity importRun = importRunEntity();
        entityManager.persist(importRun);

        TurnoverEntity turnover = turnoverEntity(lease, importRun, TurnoverStatus.FLAGGED);
        entityManager.persist(turnover);

        ValidationIssueEntity issue = new ValidationIssueEntity(
                null, turnover, ValidationRule.MONTH_OVER_MONTH_DEVIATION, "deviation",
                ValidationIssueStatus.OPEN, null, null, null);
        entityManager.persist(issue);
        entityManager.flush();
        turnoverId = turnover.getId();
        issueId = issue.getId();
    }

    @Test
    @DisplayName("Sets the turnover to CORRECTED and resolves its open issue with the audit trail (resolution / resolvedBy / resolvedAt)")
    void correctingFlaggedTurnoverResolvesIssueWithAuditTrail() {
        Turnover corrected = reviewUseCase.correctTurnover(turnoverId, CORRECTED_AMOUNT, REASON, RESOLVED_BY);

        assertEquals(TurnoverStatus.CORRECTED, corrected.status());
        assertEquals(0, CORRECTED_AMOUNT.compareTo(corrected.amount()));

        entityManager.flush();
        entityManager.clear();

        TurnoverEntity reloadedTurnover = entityManager.find(TurnoverEntity.class, turnoverId);
        assertEquals(TurnoverStatus.CORRECTED, reloadedTurnover.getStatus());
        assertEquals(0, CORRECTED_AMOUNT.compareTo(reloadedTurnover.getAmount()));

        ValidationIssueEntity reloadedIssue = entityManager.find(ValidationIssueEntity.class, issueId);
        assertEquals(ValidationIssueStatus.RESOLVED, reloadedIssue.getStatus());
        assertEquals(REASON, reloadedIssue.getResolution());
        assertEquals(RESOLVED_BY, reloadedIssue.getResolvedBy());
        assertNotNull(reloadedIssue.getResolvedAt());
    }
}
