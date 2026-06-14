package com.turnover.validation.adapter.out.tenantapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Builds the {@link TenantApiClient} proxy over a RestClient configured with the
 * Tenant App base URL and bearer authentication.
 */
@Configuration
public class TenantApiClientConfig {

    @Bean
    public TenantApiClient tenantApiClient(TenantApiProperties properties) {
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(TenantApiClient.class);
    }
}
