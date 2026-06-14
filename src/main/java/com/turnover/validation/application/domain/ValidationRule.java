package com.turnover.validation.application.domain;

/**
 * The validation rules that can raise a {@link ValidationIssue} against a turnover.
 * Each value corresponds to a rule implemented in {@link TurnoverManager}.
 */
public enum ValidationRule {

    /** Turnover deviates from the previous month beyond the deviation threshold. */
    MONTH_OVER_MONTH_DEVIATION
}
