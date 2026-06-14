package com.turnover.validation.application.domain;

import java.time.Instant;

public record ValidationIssue(
        Long id,
        Turnover turnover,
        String rule,
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
