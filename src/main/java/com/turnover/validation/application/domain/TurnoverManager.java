package com.turnover.validation.application.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain rule engine for turnover validation: detects significant month-over-month
 * deviation and raises a validation issue for review. Pure logic — no persistence;
 * the application services orchestrate loading and saving.
 */
@Component
public class TurnoverManager {

    /** Flag a turnover when it deviates from the previous month by more than this fraction. */
    public static final BigDecimal DEVIATION_THRESHOLD = new BigDecimal("0.30");
    public static final String DEVIATION_RULE = "MONTH_OVER_MONTH_DEVIATION";
    public static final String ONLY_FLAGGED_CAN_BE_REVIEWED =
            "Only a flagged turnover can be corrected or accepted";
    private static final String DEVIATION_DESCRIPTION =
            "Turnover %s for %s deviates from previous month (%s) beyond the %s threshold";

    /**
     * Decide the status of a received turnover by comparing it to the same lease's
     * previous-month turnover. Significant deviation → FLAGGED, otherwise ACCEPTED.
     * No previous month → ACCEPTED (nothing to compare against).
     */
    public Turnover classify(Turnover received, Turnover previous) {
        BigDecimal previousAmount = previous == null ? null : previous.amount();
        return isSignificantDeviation(received.amount(), previousAmount)
                ? received.flag()
                : received.accept();
    }

    /**
     * Whether this month's turnover deviates from the previous month's by more than
     * {@link #DEVIATION_THRESHOLD}. The change is measured as the absolute relative
     * difference {@code |current - previous| / |previous|}, so both large increases
     * and large drops count.
     *
     * <p>Returns {@code false} when there is nothing meaningful to compare against:
     * either amount is {@code null}, or the previous amount is zero (which would also
     * be a division by zero).
     *
     * @param current  this month's amount
     * @param previous the previous month's amount for the same lease
     * @return {@code true} if the relative change strictly exceeds the threshold
     */
    private boolean isSignificantDeviation(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.signum() == 0) {
            return false;
        }
        BigDecimal change = current.subtract(previous).abs()
                .divide(previous.abs(), 4, RoundingMode.HALF_UP);
        return change.compareTo(DEVIATION_THRESHOLD) > 0;
    }

    public ValidationIssue raiseDeviationIssue(Turnover flagged, Turnover previous) {
        String previousAmount = previous == null ? "n/a" : previous.amount().toPlainString();
        String description = String.format(DEVIATION_DESCRIPTION,
                flagged.amount().toPlainString(), flagged.period(), previousAmount, DEVIATION_THRESHOLD);
        return new ValidationIssue(
                null, flagged, DEVIATION_RULE, description,
                ValidationIssueStatus.OPEN, null, null, null);
    }

    /**
     * Correct a turnover after Asset Manager review. Only a FLAGGED turnover can
     * be corrected.
     */
    public Validation<Turnover> correct(Turnover turnover, BigDecimal correctedAmount) {
        if (turnover.status() != TurnoverStatus.FLAGGED) {
            return Validation.error(ONLY_FLAGGED_CAN_BE_REVIEWED, ValidationErrorCode.TURNOVER_NOT_FLAGGED);
        }
        return Validation.success(turnover.correct(correctedAmount));
    }

    /**
     * Accept a turnover as-reported after Asset Manager review. Only a FLAGGED
     * turnover can be accepted.
     */
    public Validation<Turnover> accept(Turnover turnover) {
        if (turnover.status() != TurnoverStatus.FLAGGED) {
            return Validation.error(ONLY_FLAGGED_CAN_BE_REVIEWED, ValidationErrorCode.TURNOVER_NOT_FLAGGED);
        }
        return Validation.success(turnover.accept());
    }
}
