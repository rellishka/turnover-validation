package com.turnover.validation.integration;

import com.turnover.validation.adapter.in.ImportRunResponse;
import com.turnover.validation.adapter.out.persistence.ImportRunEntity;
import com.turnover.validation.adapter.out.persistence.LeaseEntity;
import com.turnover.validation.adapter.out.persistence.PropertyEntity;
import com.turnover.validation.adapter.out.persistence.TenantEntity;
import com.turnover.validation.adapter.out.persistence.TurnoverEntity;
import com.turnover.validation.adapter.out.tenantapi.TenantApiClient;
import com.turnover.validation.application.domain.ImportRunStatus;
import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.application.domain.TurnoverRepository;
import com.turnover.validation.application.domain.TurnoverStatus;
import com.turnover.validation.application.domain.ValidationIssueRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiPage;
import static com.turnover.validation.helpers.TenantApiTestValuesHelper.tenantApiTurnover;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.importRunEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.leaseEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.propertyEntity;
import static com.turnover.validation.helpers.TurnoverEntityTestValuesHelper.tenantEntity;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.AMOUNT;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.CURRENCY;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.SUBMITTED_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end import: POST /imports drives the real web -> service -> domain ->
 * persistence stack against H2, with only the external Tenant App HTTP client
 * stubbed. A deviating figure (vs the seeded previous month) must be persisted
 * FLAGGED and raise an open ValidationIssue.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurnoverImportIntegrationTest {

    private static final String IMPORTS_URL = "/imports";
    // Current month, carried by the stubbed Tenant App (tenantApiTurnover() uses AMOUNT).
    private static final BigDecimal IMPORTED_AMOUNT = AMOUNT;
    // Previous month, seeded in the DB. 100000 vs 50000 = +100% > 30% threshold → FLAGGED.
    private static final BigDecimal PREVIOUS_AMOUNT = new BigDecimal("50000");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TurnoverRepository turnoverRepository;
    @Autowired
    private ValidationIssueRepository validationIssueRepository;
    @MockitoBean
    private TenantApiClient tenantApiClient;

    @BeforeEach
    void setUp() {
        PropertyEntity property = propertyEntity();
        TenantEntity tenant = tenantEntity();
        entityManager.persist(property);
        entityManager.persist(tenant);
        LeaseEntity lease = leaseEntity(property, tenant);
        entityManager.persist(lease);
        ImportRunEntity previousRun = importRunEntity();
        entityManager.persist(previousRun);
        // Previous month, accepted: gives the deviation rule something to compare against.
        entityManager.persist(new TurnoverEntity(
                null, lease, previousRun, PERIOD.minusMonths(1), PREVIOUS_AMOUNT, CURRENCY,
                TurnoverStatus.ACCEPTED, SUBMITTED_AT));
        entityManager.flush();
    }

    @Test
    @DisplayName("Persists the imported turnover as FLAGGED and raises an open issue when it deviates > 30% from the previous month")
    void importPersistsAndFlagsDeviatingTurnover() throws Exception {
        // tenantApiTurnover() carries IMPORTED_AMOUNT for PERIOD — the current month's figure.
        when(tenantApiClient.getTurnoverPage(eq(PERIOD.toString()), eq(0), anyInt()))
                .thenReturn(tenantApiPage(0, 1, List.of(tenantApiTurnover())));

        MvcResult result = mockMvc.perform(post(IMPORTS_URL).param("period", PERIOD.toString()))
                .andExpect(status().isOk())
                .andReturn();

        ImportRunResponse importRunResponse =
                objectMapper.readValue(result.getResponse().getContentAsString(), ImportRunResponse.class);
        assertEquals(ImportRunStatus.SUCCESS, importRunResponse.status());
        assertEquals(1, importRunResponse.entriesImported());
        assertEquals(PERIOD.toString(), importRunResponse.period());

        entityManager.flush();
        entityManager.clear();

        List<Turnover> flagged = turnoverRepository.findByStatus(TurnoverStatus.FLAGGED);
        assertEquals(1, flagged.size());

        Turnover flaggedTurnover = flagged.getFirst();
        assertEquals(PERIOD, flaggedTurnover.period());
        assertEquals(0, IMPORTED_AMOUNT.compareTo(flaggedTurnover.amount()));

        assertTrue(validationIssueRepository.findOpenByTurnover(flaggedTurnover).isPresent());
    }
}
