package com.turnover.validation.application.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.SUBMITTED_AT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TURNOVER_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.importRun;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.lease;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.turnover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnoverTest {

    @Test
    void wellFormedTurnoverIsValid() {
        assertTrue(turnover(TurnoverStatus.RECEIVED).isValid());
    }

    @Test
    void turnoverWithoutLeaseIsInvalid() {
        Turnover turnover = new Turnover(
                TURNOVER_ID, null, importRun(), PERIOD, AMOUNT, CURRENCY, TurnoverStatus.RECEIVED, SUBMITTED_AT);

        assertFalse(turnover.isValid());
    }

    @Test
    void turnoverWithNegativeAmountIsInvalid() {
        assertFalse(turnover(new BigDecimal("-1"), TurnoverStatus.RECEIVED).isValid());
    }

    @Test
    void turnoverWithBlankCurrencyIsInvalid() {
        Turnover turnover = new Turnover(
                TURNOVER_ID, lease(), importRun(), PERIOD, AMOUNT, "  ", TurnoverStatus.RECEIVED, SUBMITTED_AT);

        assertFalse(turnover.isValid());
    }

    @Test
    void flagSetsStatusToFlagged() {
        assertEquals(TurnoverStatus.FLAGGED, turnover(TurnoverStatus.RECEIVED).flag().status());
    }

    @Test
    void acceptSetsStatusToAccepted() {
        assertEquals(TurnoverStatus.ACCEPTED, turnover(TurnoverStatus.FLAGGED).accept().status());
    }

    @Test
    void correctUpdatesAmountAndStatusAndKeepsOtherFields() {
        Turnover original = turnover(TurnoverStatus.FLAGGED);
        BigDecimal newAmount = new BigDecimal("123");

        Turnover corrected = original.correct(newAmount);

        assertEquals(TurnoverStatus.CORRECTED, corrected.status());
        assertEquals(newAmount, corrected.amount());
        assertEquals(original.lease(), corrected.lease());
        assertEquals(original.currency(), corrected.currency());
        assertEquals(original.period(), corrected.period());
    }
}
