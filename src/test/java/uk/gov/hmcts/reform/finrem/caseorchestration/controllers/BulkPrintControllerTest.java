package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private GenerateCoverSheetService coverSheetService;

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddress() throws Exception {
        final UUID randomId = UUID.randomUUID();
        requestContent =
                objectMapper.readTree(
                        new File(getClass()
                                .getResource("/fixtures/contested/bulk_print_consent_order_not_approved.json")
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendForBulkPrint(any(), any())).thenReturn(randomId);

        mvc.perform(
                post("/case-orchestration/bulk-print")
                        .content(requestContent.toString())
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())))
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetApp").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdApp", is(randomId.toString())));
        verify(bulkPrintService, times(2)).sendForBulkPrint(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
        verify(coverSheetService).generateApplicantCoverSheet(any(), any());
    }


    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() throws Exception {
        final UUID randomId = UUID.randomUUID();
        requestContent =
                objectMapper.readTree(
                        new File(getClass()
                                .getResource("/fixtures/contested/bulk_print_simple.json")
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendForBulkPrint(any(), any())).thenReturn(randomId);

        mvc.perform(
                post("/case-orchestration/bulk-print")
                        .content(requestContent.toString())
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())))
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetApp").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdApp", is(randomId.toString())));
        verify(bulkPrintService, times(2)).sendForBulkPrint(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
        verify(coverSheetService).generateApplicantCoverSheet(any(), any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithOnlyRespondentAddress() throws Exception {
        final UUID randomId = UUID.randomUUID();
        requestContent =
                objectMapper.readTree(
                        new File(getClass()
                                .getResource("/fixtures/contested/bulk_print_consent_order_approved.json")
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendForBulkPrint(any(), any())).thenReturn(randomId);

        mvc.perform(
                post("/case-orchestration/bulk-print")
                        .content(requestContent.toString())
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())));
        verify(bulkPrintService).sendForBulkPrint(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnCoverSheetGeneration() throws Exception {
        requestContent =
                objectMapper.readTree(
                        new File(getClass().getResource("/fixtures/contested/hwf.json").toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenThrow(feignError());
        mvc.perform(
                post("/case-orchestration/bulk-print")
                        .content(requestContent.toString())
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verifyZeroInteractions(bulkPrintService);
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnSendForBulkPrint() throws Exception {
        requestContent =
                objectMapper.readTree(
                        new File(getClass().getResource("/fixtures/contested/hwf.json").toURI()));
        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendForBulkPrint(any(), any())).thenThrow(feignError());
        mvc.perform(
                post("/case-orchestration/bulk-print")
                        .content(requestContent.toString())
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
        verify(bulkPrintService).sendForBulkPrint(any(), any());
    }
}
