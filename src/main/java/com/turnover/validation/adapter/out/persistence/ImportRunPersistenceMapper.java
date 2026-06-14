package com.turnover.validation.adapter.out.persistence;

import com.turnover.validation.application.domain.ImportRun;
import org.springframework.stereotype.Component;

@Component
public class ImportRunPersistenceMapper {

    public ImportRunEntity toEntity(ImportRun importRun) {
        return new ImportRunEntity(
                importRun.id(), importRun.startedAt(), importRun.finishedAt(), importRun.status(),
                importRun.periodFetched(), importRun.entriesImported(), importRun.errorMessage());
    }

    public ImportRun toDomain(ImportRunEntity entity) {
        return new ImportRun(
                entity.getId(), entity.getStartedAt(), entity.getFinishedAt(), entity.getStatus(),
                entity.getPeriodFetched(), entity.getEntriesImported(), entity.getErrorMessage());
    }
}
