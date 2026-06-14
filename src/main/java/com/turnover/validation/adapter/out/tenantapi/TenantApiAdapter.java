package com.turnover.validation.adapter.out.tenantapi;

import com.turnover.validation.application.domain.TurnoverSubmission;
import com.turnover.validation.application.port.out.TenantApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Outbound adapter for the Tenant App. Pages through the {@link TenantApiClient}
 * and maps the wire records to domain {@link TurnoverSubmission}s.
 *
 * <p>Transient failures (5xx, connection/read timeouts) are retried; client errors
 * (4xx) are not, since they won't succeed on retry. When retries are exhausted the
 * error propagates so the import is recorded (and logged) as a failed run.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantApiAdapter implements TenantApiPort {

    private final TenantApiClient client;
    private final TenantApiProperties properties;

    /**
     * Fetches all turnover submitted for the given period from the Tenant App,
     * paging through the API and mapping each wire record to a {@link TurnoverSubmission}.
     *
     * <p>Transient failures (5xx, connection/read timeouts) are retried with backoff;
     * 4xx client errors are not. When retries are exhausted the exception propagates so
     * the import is recorded as a failed run.
     *
     * @param period the reporting month to fetch
     * @return the turnover submissions for the period, or an empty list if there are none
     */
    @Override
    @Retryable(
            includes = {HttpServerErrorException.class, ResourceAccessException.class},
            maxRetriesString = "${tenant-api.retry.max-retries:2}",
            delayString = "${tenant-api.retry.delay-ms:1000}",
            multiplierString = "${tenant-api.retry.multiplier:2.0}",
            timeUnit = TimeUnit.MILLISECONDS)
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
