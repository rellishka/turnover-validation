package com.turnover.validation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LeaseJpaRepository extends JpaRepository<LeaseEntity, Long> {

    /**
     * Finds a lease by the Tenant App's external identifiers for its tenant and property.
     * The external ids live on the related {@code tenant} / {@code property} entities, not on
     * {@link LeaseEntity} itself, so the query navigates those {@code @ManyToOne} relations.
     */
    @Query("""
            SELECT l FROM LeaseEntity l
            WHERE l.tenant.externalId = :tenantExternalId
            AND l.property.externalId = :propertyExternalId
            """)
    Optional<LeaseEntity> findByTenantExternalIdAndPropertyExternalId(String tenantExternalId, String propertyExternalId);
}
