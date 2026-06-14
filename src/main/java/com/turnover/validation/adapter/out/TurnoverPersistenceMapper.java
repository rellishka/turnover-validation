package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.Turnover;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TurnoverPersistenceMapper {

    private final LeasePersistenceMapper leaseMapper;
    private final ImportRunPersistenceMapper importRunMapper;

    public Turnover toDomain(TurnoverEntity entity) {
        return new Turnover(
                entity.getId(),
                leaseMapper.toDomain(entity.getLease()),
                importRunMapper.toDomain(entity.getImportRun()),
                entity.getPeriod(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getSubmittedAt());
    }

    public TurnoverEntity toEntity(Turnover turnover, LeaseEntity lease, ImportRunEntity importRun) {
        return new TurnoverEntity(
                turnover.id(), lease, importRun, turnover.period(), turnover.amount(),
                turnover.currency(), turnover.status(), turnover.submittedAt());
    }
}
