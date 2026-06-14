package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.ValidationIssue;
import com.turnover.validation.application.domain.ValidationIssueStatus;
import com.turnover.validation.application.port.out.ValidationIssuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValidationIssuePersistenceAdapter implements ValidationIssuePort {

    private final ValidationIssueJpaRepository validationIssueJpaRepository;
    private final TurnoverJpaRepository turnoverJpaRepository;
    private final ValidationIssuePersistenceMapper mapper;

    @Override
    public ValidationIssue save(ValidationIssue issue) {
        TurnoverEntity turnover = turnoverJpaRepository.getReferenceById(issue.turnover().id());
        ValidationIssueEntity saved = validationIssueJpaRepository.save(mapper.toEntity(issue, turnover));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ValidationIssue> findOpenByTurnover(Turnover turnover) {
        return validationIssueJpaRepository
                .findByTurnoverIdAndStatus(turnover.id(), ValidationIssueStatus.OPEN)
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }
}
