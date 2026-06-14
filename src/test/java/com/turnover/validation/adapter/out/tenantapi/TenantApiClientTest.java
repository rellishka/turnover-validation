package com.turnover.validation.adapter.out.tenantapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiPage;
import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiTurnover;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TenantApiClientTest {

    private static final String BASE_URL = "https://tenant-app.example.com";
    private static final String API_KEY = "secret-key";
    private static final int PAGE_SIZE = 100;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private MockRestServiceServer server;
    private TenantApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY);
        server = MockRestServiceServer.bindTo(builder).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(builder.build()))
                .build();
        client = factory.createClient(TenantApiClient.class);
    }

    @Test
    void sendsAuthHeaderAndQueryParamsAndDeserializesPage() throws Exception {
        TenantApiTurnover item = tenantApiTurnover();
        TenantApiPage page = tenantApiPage(0, 1, List.of(item));
        server.expect(requestTo(startsWith(BASE_URL + "/api/v1/turnovers")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(queryParam("period", PERIOD.toString()))
                .andExpect(queryParam("page", "0"))
                .andExpect(queryParam("size", String.valueOf(PAGE_SIZE)))
                .andRespond(withSuccess(objectMapper.writeValueAsString(page), MediaType.APPLICATION_JSON));

        TenantApiPage result = client.getTurnoverPage(PERIOD.toString(), 0, PAGE_SIZE);

        assertEquals(page.totalPages(), result.totalPages());
        assertEquals(1, result.items().size());
        TenantApiTurnover resultItem = result.items().getFirst();
        assertEquals(item.tenantId(), resultItem.tenantId());
        assertEquals(item.propertyId(), resultItem.propertyId());
        assertEquals(item.currency(), resultItem.currency());
        assertEquals(0, item.turnover().compareTo(resultItem.turnover()));
        assertEquals(item.submittedAt(), resultItem.submittedAt());
        server.verify();
    }
}
