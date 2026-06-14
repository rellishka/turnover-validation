package com.turnover.validation.adapter.out.tenantapi;

import com.turnover.validation.application.domain.TurnoverSubmission;
import com.turnover.validation.application.port.out.TenantApiPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.YearMonth;
import java.util.List;

import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiPage;
import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiTurnover;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "tenant-api.retry.delay-ms=0")
class TenantApiAdapterRetryTest {

    @MockitoBean
    private TenantApiClient client;

    @Autowired
    private TenantApiPort tenantApiPort;

    @Test
    void retriesTransientFailureThenSucceeds() {
        when(client.getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
                .thenReturn(tenantApiPage(0, 1, List.of(tenantApiTurnover())));

        List<TurnoverSubmission> result = tenantApiPort.fetchTurnover(PERIOD);

        assertEquals(1, result.size());
        verify(client, times(3)).getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt());
    }

    @Test
    void propagatesAfterRetriesExhausted() {
        when(client.getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(HttpServerErrorException.class, () -> tenantApiPort.fetchTurnover(PERIOD));
        verify(client, times(3)).getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt());
    }

    @Test
    void doesNotRetryClientErrors() {
        when(client.getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> tenantApiPort.fetchTurnover(PERIOD));
        verify(client, times(1)).getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt());
    }
}
