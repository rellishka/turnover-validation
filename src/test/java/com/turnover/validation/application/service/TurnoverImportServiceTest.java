package com.turnover.validation.application.service;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.domain.ImportRunRepository;
import com.turnover.validation.application.domain.ImportRunStatus;
import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.LeaseRepository;
import com.turnover.validation.application.domain.TenantApiRepository;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverManager;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.ValidationIssue;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import com.turnover.validation.application.domain.ValidationIssueStatus;
import com.turnover.validation.application.domain.ValidationRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PROPERTY_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TENANT_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.lease;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.submission;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.turnover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoverImportServiceTest {

    @Mock
    private TenantApiRepository tenantApiRepository;
    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private TurnoverRepository turnoverRepository;
    @Mock
    private ValidationIssueRepository validationIssueRepository;
    @Mock
    private ImportRunRepository importRunRepository;
    @Mock
    private TurnoverManager turnoverManager;
    @InjectMocks
    private TurnoverImportService service;

    @Test
    void importAcceptsNonDeviatingTurnover() {
        echoSavedImportRun();
        when(tenantApiRepository.fetchTurnover(PERIOD)).thenReturn(List.of(submission()));
        when(leaseRepository.findByTenantExternalIdAndPropertyExternalId(TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID))
                .thenReturn(Optional.of(lease()));
        when(turnoverRepository.findByLeaseAndPeriod(any(Lease.class), eq(PERIOD))).thenReturn(Optional.empty());
        when(turnoverRepository.findByLeaseAndPeriod(any(Lease.class), eq(PERIOD.minusMonths(1)))).thenReturn(Optional.empty());
        Turnover accepted = turnover(TurnoverStatus.ACCEPTED);
        when(turnoverManager.classify(any(Turnover.class), isNull())).thenReturn(accepted);
        when(turnoverRepository.save(accepted)).thenReturn(accepted);

        ImportRun result = service.importTurnoverForPeriod(PERIOD);

        assertEquals(ImportRunStatus.SUCCESS, result.status());
        assertEquals(1, result.entriesImported());
        verify(validationIssueRepository, never()).save(any());
    }

    @Test
    void importFlagsDeviatingTurnoverAndRaisesIssue() {
        echoSavedImportRun();
        when(tenantApiRepository.fetchTurnover(PERIOD)).thenReturn(List.of(submission(new BigDecimal("200000"))));
        when(leaseRepository.findByTenantExternalIdAndPropertyExternalId(TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID))
                .thenReturn(Optional.of(lease()));
        when(turnoverRepository.findByLeaseAndPeriod(any(Lease.class), eq(PERIOD))).thenReturn(Optional.empty());
        Turnover previous = turnover(new BigDecimal("100000"), TurnoverStatus.ACCEPTED);
        when(turnoverRepository.findByLeaseAndPeriod(any(Lease.class), eq(PERIOD.minusMonths(1))))
                .thenReturn(Optional.of(previous));
        Turnover flagged = turnover(new BigDecimal("200000"), TurnoverStatus.FLAGGED);
        when(turnoverManager.classify(any(Turnover.class), eq(previous))).thenReturn(flagged);
        when(turnoverRepository.save(flagged)).thenReturn(flagged);
        ValidationIssue issue = new ValidationIssue(
                null, flagged, ValidationRule.MONTH_OVER_MONTH_DEVIATION, "desc", ValidationIssueStatus.OPEN, null, null, null);
        when(turnoverManager.raiseDeviationIssue(flagged, previous)).thenReturn(issue);

        ImportRun result = service.importTurnoverForPeriod(PERIOD);

        assertEquals(ImportRunStatus.SUCCESS, result.status());
        assertEquals(1, result.entriesImported());
        verify(validationIssueRepository).save(issue);
    }

    @Test
    void importSkipsSubmissionWithoutMatchingLease() {
        echoSavedImportRun();
        when(tenantApiRepository.fetchTurnover(PERIOD)).thenReturn(List.of(submission()));
        when(leaseRepository.findByTenantExternalIdAndPropertyExternalId(TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID))
                .thenReturn(Optional.empty());

        ImportRun result = service.importTurnoverForPeriod(PERIOD);

        assertEquals(ImportRunStatus.SUCCESS, result.status());
        assertEquals(0, result.entriesImported());
        verify(turnoverRepository, never()).save(any());
    }

    @Test
    void importMarksRunFailedWhenFetchThrows() {
        echoSavedImportRun();
        when(tenantApiRepository.fetchTurnover(PERIOD)).thenThrow(new RuntimeException("API down"));

        ImportRun result = service.importTurnoverForPeriod(PERIOD);

        assertEquals(ImportRunStatus.FAILED, result.status());
        assertEquals("API down", result.errorMessage());
    }

    /** ImportRun repository returns whatever ImportRun it is asked to save. */
    private void echoSavedImportRun() {
        when(importRunRepository.save(any(ImportRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
