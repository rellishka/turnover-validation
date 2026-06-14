package com.turnover.validation.adapter.out.tenantapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the external Tenant App REST API. The API key is sourced from
 * the environment (a secret)
 */
@ConfigurationProperties(prefix = "tenant-api")
public record TenantApiProperties(String baseUrl, String apiKey, int pageSize) {

    public TenantApiProperties {
        if (pageSize <= 0) {
            pageSize = 100;
        }
    }
}
