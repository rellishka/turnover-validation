package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.ValidationIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationIssuePersistenceMapper {

    private final TurnoverPersistenceMapper turnoverMapper;

    public ValidationIssue toDomain(ValidationIssueEntity entity) {
        return new ValidationIssue(
                entity.getId(),
                turnoverMapper.toDomain(entity.getTurnover()),
                entity.getRule(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getResolution(),
                entity.getResolvedBy(),
                entity.getResolvedAt());
    }

    public ValidationIssueEntity toEntity(ValidationIssue issue, TurnoverEntity turnover) {
        return new ValidationIssueEntity(
                issue.id(), turnover, issue.rule(), issue.description(), issue.status(),
                issue.resolution(), issue.resolvedBy(), issue.resolvedAt());
    }
}
