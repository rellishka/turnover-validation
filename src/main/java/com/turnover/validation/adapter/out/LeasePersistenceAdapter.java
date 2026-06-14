package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.port.out.LeasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeasePersistenceAdapter implements LeasePort {

    private final LeaseJpaRepository leaseJpaRepository;
    private final LeasePersistenceMapper mapper;

    @Override
    public Optional<Lease> findByTenantExternalIdAndPropertyExternalId(String tenantExternalId, String propertyExternalId) {
        return leaseJpaRepository
                .findByTenantExternalIdAndPropertyExternalId(tenantExternalId, propertyExternalId)
                .map(mapper::toDomain);
    }
}
