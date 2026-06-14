package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.port.out.TurnoverPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TurnoverPersistenceAdapter implements TurnoverPort {

    private final TurnoverJpaRepository turnoverJpaRepository;
    private final LeaseJpaRepository leaseJpaRepository;
    private final ImportRunJpaRepository importRunJpaRepository;
    private final TurnoverPersistenceMapper mapper;

    @Override
    public Turnover save(Turnover turnover) {
        LeaseEntity lease = leaseJpaRepository.getReferenceById(turnover.lease().id());
        ImportRunEntity importRun = importRunJpaRepository.getReferenceById(turnover.importRun().id());
        TurnoverEntity saved = turnoverJpaRepository.save(mapper.toEntity(turnover, lease, importRun));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Turnover> findById(Long id) {
        return turnoverJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Turnover> findByLeaseAndPeriod(Lease lease, YearMonth period) {
        return turnoverJpaRepository.findByLeaseIdAndPeriod(lease.id(), period)
                .map(mapper::toDomain);
    }

    @Override
    public List<Turnover> findByStatus(TurnoverStatus status) {
        return turnoverJpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
