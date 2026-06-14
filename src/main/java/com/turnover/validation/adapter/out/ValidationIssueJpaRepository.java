package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.ValidationIssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ValidationIssueJpaRepository extends JpaRepository<ValidationIssueEntity, Long> {

    /**
     * Finds validation issues for a given turnover with the given status. Navigates the
     * {@code turnover} {@code @ManyToOne} relation to match on its id.
     */
    @Query("""
            SELECT i FROM ValidationIssueEntity i
            WHERE i.turnover.id = :turnoverId
            AND i.status = :status
            """)
    List<ValidationIssueEntity> findByTurnoverIdAndStatus(Long turnoverId, ValidationIssueStatus status);
}
