package com.turnover.validation.adapter.in;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.port.in.TurnoverImportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * Inbound HTTP adapter for manually triggering a turnover import, e.g. to recover
 * a failed run after a fix or to re-fetch a period on demand.
 *
 * <p>The import runs synchronously and records its own outcome as an
 * {@link ImportRun} (it does not throw on a business failure), so the endpoint
 * returns {@code 200 OK} with the run in the body — a failed import comes back
 * with {@code status: FAILED} and an error message for the caller to inspect.
 */
@Slf4j
@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
public class TurnoverImportController {

    private final TurnoverImportUseCase turnoverImportUseCase;
    private final ImportRunDtoMapper importRunDtoMapper;

    @PostMapping
    public ResponseEntity<ImportRunResponse> triggerImport(@RequestParam YearMonth period) {
        log.info("Manual import triggered for period {}", period);

        ImportRun importRun = turnoverImportUseCase.importTurnoverForPeriod(period);

        return ResponseEntity.ok(importRunDtoMapper.toResponse(importRun));
    }
}
