package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.domain.ImportRunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({ImportRunPersistenceAdapter.class, ImportRunPersistenceMapper.class})
class ImportRunPersistenceAdapterTest {

    @Autowired
    private ImportRunPersistenceAdapter adapter;

    @Test
    void saveAssignsIdAndRoundTripsFields() {
        ImportRun saved = adapter.save(new ImportRun(PERIOD));

        assertNotNull(saved.id());
        assertEquals(ImportRunStatus.RUNNING, saved.status());
        assertEquals(PERIOD, saved.periodFetched());
    }
}
