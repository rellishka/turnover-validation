package com.turnover.validation.adapter.out.tenantapi;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative HTTP client (contract) for the external Tenant App turnover endpoint.
 * Spring generates the implementation; transport configuration (base URL, auth) lives
 * in {@link TenantApiClientConfig}. Returns the raw wire page; the adapter pages and
 * maps to the domain.
 */
@HttpExchange("/api/v1/turnovers")
public interface TenantApiClient {

    @GetExchange
    TenantApiPage getTurnoverPage(@RequestParam("period") String period,
                                  @RequestParam("page") int page,
                                  @RequestParam("size") int size);
}
