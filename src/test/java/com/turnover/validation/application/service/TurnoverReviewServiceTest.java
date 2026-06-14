package com.turnover.validation.application.service;

import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverManager;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.Validation;
import com.turnover.validation.application.domain.ValidationErrorCode;
import com.turnover.validation.application.domain.ValidationIssue;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import com.turnover.validation.application.domain.ValidationIssueStatus;
import com.turnover.validation.application.domain.ValidationRule;
import com.turnover.validation.exception.InvalidTurnoverException;
import com.turnover.validation.exception.TurnoverNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TURNOVER_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.turnover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoverReviewServiceTest {

    @Mock
    private TurnoverRepository turnoverRepository;
    @Mock
    private ValidationIssueRepository validationIssueRepository;
    @Mock
    private TurnoverManager turnoverManager;
    @InjectMocks
    private TurnoverReviewService service;

    @Test
    void getFlaggedTurnoversReturnsFlagged() {
        List<Turnover> flagged = List.of(turnover(TurnoverStatus.FLAGGED));
        when(turnoverRepository.findByStatus(TurnoverStatus.FLAGGED)).thenReturn(flagged);

        assertEquals(flagged, service.getFlaggedTurnovers());
    }

    @Test
    void correctTurnoverSavesCorrectionAndResolvesOpenIssue() {
        Turnover flagged = turnover(TurnoverStatus.FLAGGED);
        BigDecimal correctedAmount = new BigDecimal("90000");
        Turnover corrected = flagged.correct(correctedAmount);
        ValidationIssue openIssue = openIssueFor(flagged);

        when(turnoverRepository.findById(TURNOVER_ID)).thenReturn(Optional.of(flagged));
        when(turnoverManager.correct(flagged, correctedAmount)).thenReturn(Validation.success(corrected));
        when(turnoverRepository.save(corrected)).thenReturn(corrected);
        when(validationIssueRepository.findOpenByTurnover(flagged)).thenReturn(Optional.of(openIssue));

        Turnover result = service.correctTurnover(TURNOVER_ID, correctedAmount, "tenant confirmed figure", "alice");

        assertEquals(corrected, result);
        verify(validationIssueRepository).save(any(ValidationIssue.class));
    }

    @Test
    void correctTurnoverThrowsWhenTurnoverNotFound() {
        when(turnoverRepository.findById(TURNOVER_ID)).thenReturn(Optional.empty());

        assertThrows(TurnoverNotFoundException.class,
                () -> service.correctTurnover(TURNOVER_ID, BigDecimal.TEN, "reason", "alice"));
    }

    @Test
    void correctTurnoverThrowsInvalidWhenNotFlagged() {
        Turnover accepted = turnover(TurnoverStatus.ACCEPTED);
        when(turnoverRepository.findById(TURNOVER_ID)).thenReturn(Optional.of(accepted));
        when(turnoverManager.correct(accepted, BigDecimal.TEN))
                .thenReturn(Validation.error("not flagged", ValidationErrorCode.TURNOVER_NOT_FLAGGED));

        assertThrows(InvalidTurnoverException.class,
                () -> service.correctTurnover(TURNOVER_ID, BigDecimal.TEN, "reason", "alice"));
        verify(turnoverRepository, never()).save(any());
    }

    @Test
    void acceptTurnoverSavesAcceptanceAndResolvesOpenIssue() {
        Turnover flagged = turnover(TurnoverStatus.FLAGGED);
        Turnover accepted = flagged.accept();
        ValidationIssue openIssue = openIssueFor(flagged);

        when(turnoverRepository.findById(TURNOVER_ID)).thenReturn(Optional.of(flagged));
        when(turnoverManager.accept(flagged)).thenReturn(Validation.success(accepted));
        when(turnoverRepository.save(accepted)).thenReturn(accepted);
        when(validationIssueRepository.findOpenByTurnover(flagged)).thenReturn(Optional.of(openIssue));

        Turnover result = service.acceptTurnover(TURNOVER_ID, "figure confirmed correct", "alice");

        assertEquals(accepted, result);
        verify(validationIssueRepository).save(any(ValidationIssue.class));
    }

    private ValidationIssue openIssueFor(Turnover turnover) {
        return new ValidationIssue(
                1L, turnover, ValidationRule.MONTH_OVER_MONTH_DEVIATION, "description", ValidationIssueStatus.OPEN, null, null, null);
    }
}
