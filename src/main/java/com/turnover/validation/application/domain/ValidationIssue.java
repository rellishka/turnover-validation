package com.turnover.validation.application.domain;

import java.time.Instant;

/**
 * An issue raised against a turnover by a validation rule (e.g. a significant
 * month-over-month deviation), tracked until an Asset Manager resolves it.
 *
 * <p>The {@code rule} is a stable identifier for which validation rule fired —
 * machine-oriented, for grouping and filtering, and unchanged even if wording
 * changes. The {@code description} is the human-readable explanation of this
 * specific occurrence, shown to the Asset Manager on the review screen. For a
 * deviation issue they are saved as, for example:
 * <pre>
 *   rule        = ValidationRule.MONTH_OVER_MONTH_DEVIATION
 *   description = "Turnover 266000 for 2026-05 deviates from previous month (180000) beyond the 0.30 threshold"
 * </pre>
 *
 * <p>The {@code resolution} captures <em>why</em> the issue was closed — the reason
 * the Asset Manager gave when correcting or accepting the figure. Together with
 * {@code resolvedBy} and {@code resolvedAt} it forms the audit trail that lets the
 * Board trust the validated numbers.
 */
public record ValidationIssue(
        Long id,
        Turnover turnover,
        ValidationRule rule,
        String description,
        ValidationIssueStatus status,
        String resolution,
        String resolvedBy,
        Instant resolvedAt
) {

    /** This issue was resolved by an Asset Manager. */
    public ValidationIssue resolve(String resolution, String resolvedBy, Instant when) {
        return new ValidationIssue(
                id, turnover, rule, description,
                ValidationIssueStatus.RESOLVED, resolution, resolvedBy, when);
    }
}
