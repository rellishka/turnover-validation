package com.turnover.validation.application.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.turnover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnoverManagerTest {

    private final TurnoverManager manager = new TurnoverManager();

    @Test
    void classifyAcceptsWhenNoPreviousMonth() {
        Turnover received = turnover(TurnoverStatus.RECEIVED);

        Turnover result = manager.classify(received, null);

        assertEquals(TurnoverStatus.ACCEPTED, result.status());
    }

    @Test
    void classifyAcceptsWhenChangeIsWithinThreshold() {
        Turnover received = turnover(new BigDecimal("120000"), TurnoverStatus.RECEIVED); // +20%
        Turnover previous = turnover(new BigDecimal("100000"), TurnoverStatus.ACCEPTED);

        Turnover result = manager.classify(received, previous);

        assertEquals(TurnoverStatus.ACCEPTED, result.status());
    }

    @Test
    void classifyFlagsWhenIncreaseExceedsThreshold() {
        Turnover received = turnover(new BigDecimal("140000"), TurnoverStatus.RECEIVED); // +40%
        Turnover previous = turnover(new BigDecimal("100000"), TurnoverStatus.ACCEPTED);

        Turnover result = manager.classify(received, previous);

        assertEquals(TurnoverStatus.FLAGGED, result.status());
    }

    @Test
    void classifyFlagsWhenDropExceedsThreshold() {
        Turnover received = turnover(new BigDecimal("50000"), TurnoverStatus.RECEIVED); // -50%
        Turnover previous = turnover(new BigDecimal("100000"), TurnoverStatus.ACCEPTED);

        Turnover result = manager.classify(received, previous);

        assertEquals(TurnoverStatus.FLAGGED, result.status());
    }

    @Test
    void correctSucceedsForFlaggedTurnover() {
        Turnover flagged = turnover(TurnoverStatus.FLAGGED);
        BigDecimal correctedAmount = new BigDecimal("90000");

        Validation<Turnover> validation = manager.correct(flagged, correctedAmount);

        assertTrue(validation.isValid());
        Turnover result = validation.getObject().orElseThrow();
        assertEquals(TurnoverStatus.CORRECTED, result.status());
        assertEquals(correctedAmount, result.amount());
    }

    @Test
    void correctFailsForNonFlaggedTurnover() {
        Turnover accepted = turnover(TurnoverStatus.ACCEPTED);

        Validation<Turnover> validation = manager.correct(accepted, new BigDecimal("90000"));

        assertFalse(validation.isValid());
        assertEquals(ValidationErrorCode.TURNOVER_NOT_FLAGGED, validation.getValidationErrorCode());
    }

    @Test
    void acceptSucceedsForFlaggedTurnover() {
        Turnover flagged = turnover(TurnoverStatus.FLAGGED);

        Validation<Turnover> validation = manager.accept(flagged);

        assertTrue(validation.isValid());
        assertEquals(TurnoverStatus.ACCEPTED, validation.getObject().orElseThrow().status());
    }

    @Test
    void acceptFailsForNonFlaggedTurnover() {
        Validation<Turnover> validation = manager.accept(turnover(TurnoverStatus.ACCEPTED));

        assertFalse(validation.isValid());
        assertEquals(ValidationErrorCode.TURNOVER_NOT_FLAGGED, validation.getValidationErrorCode());
    }

    @Test
    void raiseDeviationIssueCreatesOpenIssueForTheTurnover() {
        Turnover flagged = turnover(TurnoverStatus.FLAGGED);
        Turnover previous = turnover(new BigDecimal("100000"), TurnoverStatus.ACCEPTED);

        ValidationIssue issue = manager.raiseDeviationIssue(flagged, previous);

        assertEquals(ValidationIssueStatus.OPEN, issue.status());
        assertEquals(ValidationRule.MONTH_OVER_MONTH_DEVIATION, issue.rule());
        assertSame(flagged, issue.turnover());
        assertNotNull(issue.description());
    }
}
