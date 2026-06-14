package com.turnover.validation;

import com.turnover.validation.application.domain.TenantApiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ValidationApplicationTests {

    // The Tenant API adapter (REST client) isn't implemented yet; mock its port so
    // the context can load. The persistence ports are now backed by real adapters.
    @MockitoBean
    private TenantApiRepository tenantApiRepository;

    @Test
    void contextLoads() {
    }

}
