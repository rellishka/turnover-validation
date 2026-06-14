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
}
