package com.turnover.validation.application.domain;

import java.util.Optional;

public interface ValidationIssueRepository {

    ValidationIssue save(ValidationIssue issue);

    Optional<ValidationIssue> findOpenByTurnover(Turnover turnover);
}
