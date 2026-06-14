package com.turnover.validation;

import com.turnover.validation.application.domain.ImportRunRepository;
import com.turnover.validation.application.domain.LeaseRepository;
import com.turnover.validation.application.domain.TenantApiRepository;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ValidationApplicationTests {

    // Outbound ports have no adapter.out implementation yet; mock them so the
    // application context (services + manager wiring) can load. Remove once the
    // persistence and Tenant API adapters provide real beans.
    @MockitoBean
    private TurnoverRepository turnoverRepository;
    @MockitoBean
    private LeaseRepository leaseRepository;
    @MockitoBean
    private ValidationIssueRepository validationIssueRepository;
    @MockitoBean
    private ImportRunRepository importRunRepository;
    @MockitoBean
    private TenantApiRepository tenantApiRepository;

    @Test
    void contextLoads() {
    }

}
