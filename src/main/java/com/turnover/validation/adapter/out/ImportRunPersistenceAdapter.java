package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.port.out.ImportRunPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportRunPersistenceAdapter implements ImportRunPort {

    private final ImportRunJpaRepository importRunJpaRepository;
    private final ImportRunPersistenceMapper mapper;

    @Override
    public ImportRun save(ImportRun importRun) {
        ImportRunEntity saved = importRunJpaRepository.save(mapper.toEntity(importRun));
        return mapper.toDomain(saved);
    }
}
