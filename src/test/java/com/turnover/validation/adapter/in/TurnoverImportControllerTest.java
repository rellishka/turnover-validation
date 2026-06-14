package com.turnover.validation.adapter.in;

import com.turnover.validation.adapter.in.exceptionhandler.ErrorResponse;
import com.turnover.validation.adapter.in.exceptionhandler.GenericExceptionHandler;
import com.turnover.validation.application.domain.ImportRun;
import com.turnover.validation.application.port.in.TurnoverImportUseCase;
import com.turnover.validation.exception.GenericException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static com.turnover.validation.helpers.TurnoverTestValuesHelper.PERIOD;
import static com.turnover.validation.helpers.TurnoverTestValuesHelper.succeededImportRun;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TurnoverImportController.class)
@Import({
        ImportRunDtoMapper.class,
        GenericExceptionHandler.class
})
class TurnoverImportControllerTest {

    private static final String IMPORTS_URL = "/imports";
    private static final String PERIOD_PARAM = "period";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TurnoverImportUseCase turnoverImportUseCase;

    @Test
    void triggerImportReturnsOkWithImportRun() throws Exception {
        ImportRun importRun = succeededImportRun();
        when(turnoverImportUseCase.importTurnoverForPeriod(PERIOD)).thenReturn(importRun);

        MvcResult result = mockMvc.perform(post(IMPORTS_URL)
                        .param(PERIOD_PARAM, PERIOD.toString()))
                .andExpect(status().isOk())
                .andReturn();

        ImportRunResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), ImportRunResponse.class);

        assertEquals(importRun.id(), response.id());
        assertEquals(importRun.status(), response.status());
        assertEquals(importRun.periodFetched().toString(), response.period());
        assertEquals(importRun.entriesImported(), response.entriesImported());
        assertEquals(importRun.startedAt(), response.startedAt());
        assertEquals(importRun.finishedAt(), response.finishedAt());
        assertEquals(importRun.errorMessage(), response.errorMessage());
    }

    @Test
    void triggerImportReturnsInternalServerErrorWhenGenericExceptionIsThrown() throws Exception {
        String message = "Unexpected error";
        when(turnoverImportUseCase.importTurnoverForPeriod(PERIOD)).thenThrow(new GenericException(message));

        MvcResult result = mockMvc.perform(post(IMPORTS_URL)
                        .param(PERIOD_PARAM, PERIOD.toString()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ErrorResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);

        assertEquals(message, response.message());
    }
}
