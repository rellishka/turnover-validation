package com.turnover.validation.application.domain;

import java.util.Optional;

public interface LeaseRepository {

    Optional<Lease> findByTenantExternalIdAndPropertyExternalId(String tenantExternalId, String propertyExternalId);
}
