package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.TurnoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface TurnoverJpaRepository extends JpaRepository<TurnoverEntity, Long> {

    /**
     * Finds the turnover for a given lease and reporting period. Navigates the
     * {@code lease} {@code @ManyToOne} relation to match on its id.
     */
    @Query("""
            SELECT t FROM TurnoverEntity t
            WHERE t.lease.id = :leaseId
            AND t.period = :period
            """)
    Optional<TurnoverEntity> findByLeaseIdAndPeriod(Long leaseId, YearMonth period);

    List<TurnoverEntity> findByStatus(TurnoverStatus status);
}
