package com.turnover.validation.adapter.out.tenantapi;

import com.turnover.validation.application.domain.TurnoverSubmission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiPage;
import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiTurnover;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantApiAdapterTest {

    private static final int PAGE_SIZE = 100;

    @Mock
    private TenantApiClient client;

    private TenantApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TenantApiAdapter(client, new TenantApiProperties("http://tenant-app", "key", PAGE_SIZE));
    }

    @Test
    void mapsWireRecordsToDomainSubmissions() {
        TenantApiTurnover wire = tenantApiTurnover();
        when(client.getTurnoverPage(PERIOD.toString(), 0, PAGE_SIZE)).thenReturn(tenantApiPage(0, 1, List.of(wire)));

        List<TurnoverSubmission> result = adapter.fetchTurnover(PERIOD);

        assertEquals(1, result.size());
        TurnoverSubmission submission = result.getFirst();
        assertEquals(wire.tenantId(), submission.tenantExternalId());
        assertEquals(wire.propertyId(), submission.propertyExternalId());
        assertEquals(PERIOD, submission.period());
        assertEquals(wire.currency(), submission.currency());
        assertEquals(0, wire.turnover().compareTo(submission.amount()));
        assertEquals(wire.submittedAt(), submission.submittedAt());
    }

    @Test
    void pagesThroughAllResults() {
        TenantApiTurnover first = tenantApiTurnover("T-1", "P-1");
        TenantApiTurnover second = tenantApiTurnover("T-2", "P-2");
        when(client.getTurnoverPage(PERIOD.toString(), 0, PAGE_SIZE)).thenReturn(tenantApiPage(0, 2, List.of(first)));
        when(client.getTurnoverPage(PERIOD.toString(), 1, PAGE_SIZE)).thenReturn(tenantApiPage(1, 2, List.of(second)));

        List<TurnoverSubmission> result = adapter.fetchTurnover(PERIOD);

        assertEquals(2, result.size());
        assertEquals(first.tenantId(), result.get(0).tenantExternalId());
        assertEquals(second.tenantId(), result.get(1).tenantExternalId());
    }
}
