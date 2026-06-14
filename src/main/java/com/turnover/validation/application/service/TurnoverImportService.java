package com.turnover.validation.application.service;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.domain.ImportRunRepository;
import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.LeaseRepository;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverManager;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.TenantApiRepository;
import com.turnover.validation.application.domain.TurnoverSubmission;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import com.turnover.validation.application.port.in.TurnoverImportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoverImportService implements TurnoverImportUseCase {

    private final TenantApiRepository tenantApiRepository;
    private final LeaseRepository leaseRepository;
    private final TurnoverRepository turnoverRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final ImportRunRepository importRunRepository;
    private final TurnoverManager turnoverManager;

    @Override
    @Transactional
    public ImportRun importTurnoverForPeriod(YearMonth period) {
        ImportRun importRun = importRunRepository.save(new ImportRun(period));
        log.info("Import started for period {} (importRun {})", period, importRun.id());

        try {
            int imported = (int) tenantApiRepository.fetchTurnover(period).stream()
                    .filter(submission -> importSubmission(submission, importRun))
                    .count();
            log.info("Import finished for period {}: {} entries", period, imported);
            return importRunRepository.save(importRun.succeeded(imported));
        } catch (RuntimeException e) {
            log.error("Import failed for period {}", period, e);
            return importRunRepository.save(importRun.failed(e.getMessage()));
        }
    }

    private boolean importSubmission(TurnoverSubmission submission, ImportRun run) {
        Optional<Lease> lease = leaseRepository.findByTenantExternalIdAndPropertyExternalId(
                submission.tenantExternalId(), submission.propertyExternalId());
        if (lease.isEmpty()) {
            log.warn("No lease for tenant {} / property {}, skipping",
                    submission.tenantExternalId(), submission.propertyExternalId());
            return false;
        }

        // Idempotency: one turnover per lease per period.
        if (turnoverRepository.findByLeaseAndPeriod(lease.get(), submission.period()).isPresent()) {
            log.debug("Turnover already imported for lease {} period {}, skipping",
                    lease.get().id(), submission.period());
            return false;
        }

        Turnover received = new Turnover(
                null, lease.get(), run, submission.period(), submission.amount(),
                submission.currency(), TurnoverStatus.RECEIVED, submission.submittedAt());
        if (!received.isValid()) {
            log.warn("Invalid turnover submission for lease {} period {}, skipping",
                    lease.get().id(), submission.period());
            return false;
        }

        Turnover previous = turnoverRepository
                .findByLeaseAndPeriod(lease.get(), submission.period().minusMonths(1))
                .orElse(null);
        Turnover classified = turnoverManager.classify(received, previous);
        Turnover saved = turnoverRepository.save(classified);

        if (saved.status() == TurnoverStatus.FLAGGED) {
            validationIssueRepository.save(turnoverManager.raiseDeviationIssue(saved, previous));
        }
        return true;
    }
}
