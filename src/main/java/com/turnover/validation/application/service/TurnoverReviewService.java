package com.turnover.validation.application.service;

import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverManager;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.Validation;
import com.turnover.validation.application.domain.ValidationErrorCode;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import com.turnover.validation.application.port.in.TurnoverReviewUseCase;
import com.turnover.validation.exception.GenericException;
import com.turnover.validation.exception.InvalidTurnoverException;
import com.turnover.validation.exception.TurnoverNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoverReviewService implements TurnoverReviewUseCase {

    public static final String TURNOVER_NOT_FOUND = "Turnover not found: %d";
    public static final String UNEXPECTED_VALIDATION_STATE = "Unexpected validation state";

    private final TurnoverRepository turnoverRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final TurnoverManager turnoverManager;

    @Override
    public List<Turnover> getFlaggedTurnovers() {
        return turnoverRepository.findByStatus(TurnoverStatus.FLAGGED);
    }

    @Override
    @Transactional
    public Turnover correctTurnover(Long turnoverId, BigDecimal correctedAmount, String reason, String resolvedBy) {
        Turnover turnover = findOrThrow(turnoverId);
        Validation<Turnover> validation = turnoverManager.correct(turnover, correctedAmount);
        Turnover corrected = unwrap(validation);
        Turnover saved = turnoverRepository.save(corrected);
        resolveOpenIssue(turnover, reason, resolvedBy);
        log.info("Turnover {} corrected by {}", turnoverId, resolvedBy);
        return saved;
    }

    @Override
    @Transactional
    public Turnover acceptTurnover(Long turnoverId, String reason, String resolvedBy) {
        Turnover turnover = findOrThrow(turnoverId);
        Validation<Turnover> validation = turnoverManager.accept(turnover);
        Turnover accepted = unwrap(validation);
        Turnover saved = turnoverRepository.save(accepted);
        resolveOpenIssue(turnover, reason, resolvedBy);
        log.info("Turnover {} accepted by {}", turnoverId, resolvedBy);
        return saved;
    }

    private Turnover findOrThrow(Long turnoverId) {
        return turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new TurnoverNotFoundException(String.format(TURNOVER_NOT_FOUND, turnoverId)));
    }

    private void resolveOpenIssue(Turnover turnover, String reason, String resolvedBy) {
        validationIssueRepository.findOpenByTurnover(turnover)
                .ifPresent(issue -> validationIssueRepository.save(
                        issue.resolve(reason, resolvedBy, Instant.now())));
    }

    private Turnover unwrap(Validation<Turnover> validation) {
        return validation.getObject().orElseThrow(() -> mapError(validation));
    }

    private GenericException mapError(Validation<Turnover> validation) {
        String message = validation.getMessage().orElse(UNEXPECTED_VALIDATION_STATE);
        ValidationErrorCode code = validation.getValidationErrorCode();
        if (code == null) {
            return new GenericException(message);
        }
        return switch (code) {
            case TURNOVER_NOT_FLAGGED, INVALID_TURNOVER -> new InvalidTurnoverException(message);
            case GENERIC -> new GenericException(message);
        };
    }
}
