package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.Lease;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.leaseEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.propertyEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.tenantEntity;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PROPERTY_EXTERNAL_ID;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.TENANT_EXTERNAL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({LeasePersistenceAdapter.class, LeasePersistenceMapper.class})
class LeasePersistenceAdapterTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private LeasePersistenceAdapter adapter;

    @Test
    void findsLeaseByTenantAndPropertyExternalIds() {
        PropertyEntity property = entityManager.persistFlushFind(propertyEntity());
        TenantEntity tenant = entityManager.persistFlushFind(tenantEntity());
        entityManager.persistFlushFind(leaseEntity(property, tenant));

        Optional<Lease> found = adapter.findByTenantExternalIdAndPropertyExternalId(
                TENANT_EXTERNAL_ID, PROPERTY_EXTERNAL_ID);

        assertTrue(found.isPresent());
        assertEquals(PROPERTY_EXTERNAL_ID, found.orElseThrow().property().externalId());
        assertEquals(TENANT_EXTERNAL_ID, found.orElseThrow().tenant().externalId());
    }

    @Test
    void returnsEmptyWhenNoLeaseMatches() {
        Optional<Lease> found = adapter.findByTenantExternalIdAndPropertyExternalId("unknown", "unknown");

        assertTrue(found.isEmpty());
    }
}
