package com.turnover.validation.adapter.out.tenantapi;

import com.turnover.validation.application.domain.TurnoverSubmission;
import com.turnover.validation.application.port.out.TenantApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Outbound adapter for the Tenant App. Pages through the {@link TenantApiClient}
 * and maps the wire records to domain {@link TurnoverSubmission}s.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantApiAdapter implements TenantApiPort {

    private final TenantApiClient client;
    private final TenantApiProperties properties;

    @Override
    public List<TurnoverSubmission> fetchTurnover(YearMonth period) {
        List<TurnoverSubmission> submissions = new ArrayList<>();
        int page = 0;
        int totalPages = 1;
        while (page < totalPages) {
            TenantApiPage response = client.getTurnoverPage(period.toString(), page, properties.pageSize());
            if (response == null) {
                break;
            }
            response.items().forEach(item -> submissions.add(toSubmission(item)));
            totalPages = response.totalPages();
            page++;
        }
        log.info("Fetched {} turnover submissions for period {}", submissions.size(), period);
        return submissions;
    }

    private TurnoverSubmission toSubmission(TenantApiTurnover item) {
        return new TurnoverSubmission(
                item.tenantId(),
                item.propertyId(),
                YearMonth.parse(item.period()),
                item.turnover(),
                item.currency(),
                item.submittedAt());
    }
}
