package com.turnover.validation.application.domain;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface TurnoverRepository {

    Turnover save(Turnover turnover);

    Optional<Turnover> findById(Long id);

    Optional<Turnover> findByLeaseAndPeriod(Lease lease, YearMonth period);

    List<Turnover> findByStatus(TurnoverStatus status);
}
