package com.turnover.validation.adapter.in;

import com.turnover.validation.application.domain.ImportRun;
import org.springframework.stereotype.Component;

/**
 * Maps the domain {@link ImportRun} to its HTTP response DTO, keeping the domain
 * object out of the inbound adapter's wire contract.
 */
@Component
public class ImportRunDtoMapper {

    public ImportRunResponse toResponse(ImportRun importRun) {
        return new ImportRunResponse(
                importRun.id(),
                importRun.status(),
                importRun.periodFetched().toString(),
                importRun.entriesImported(),
                importRun.startedAt(),
                importRun.finishedAt(),
                importRun.errorMessage());
    }
}
