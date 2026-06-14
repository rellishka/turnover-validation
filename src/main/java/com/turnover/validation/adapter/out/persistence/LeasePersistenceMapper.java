package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.Lease;
import com.turnover.validation.application.domain.Property;
import com.turnover.validation.application.domain.Tenant;
import org.springframework.stereotype.Component;

@Component
public class LeasePersistenceMapper {

    public Lease toDomain(LeaseEntity entity) {
        return new Lease(
                entity.getId(),
                toDomain(entity.getProperty()),
                toDomain(entity.getTenant()),
                entity.getStartDate(),
                entity.getEndDate());
    }

    private Property toDomain(PropertyEntity entity) {
        return new Property(entity.getId(), entity.getExternalId(), entity.getName(), entity.getCountry(), entity.getCity());
    }

    private Tenant toDomain(TenantEntity entity) {
        return new Tenant(entity.getId(), entity.getExternalId(), entity.getName());
    }
}
