package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(BulkPrintController.class)
public class BulkPrintControllerTest extends BaseControllerTest {

    private static final String BULK_PRINT_URI = "/case-orchestration/bulk-print";
    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONSENTED_BULK_PRINT_SIMPLE_JSON = "/fixtures/contested/bulk_print_simple.json";
    private static final String CONSENTED_BULK_PRINT_CONSENT_ORDER_APPROVED_JSON
        = "/fixtures/contested/bulk_print_consent_order_approved.json";
    private static final String CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON
        = "/fixtures/contested/bulk_print_consent_order_not_approved.json";

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
                                .getResource(CONSENTED_BULK_PRINT_CONSENT_ORDER_NOT_APPROVED_JSON)
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.printApplicantConsentOrderNotApprovedDocuments(any(), any())).thenReturn(randomId);
        when(bulkPrintService.sendOrderForBulkPrintRespondent(any(), any())).thenReturn(randomId);

        mvc.perform(
                post(BULK_PRINT_URI)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())))
                .andExpect(jsonPath("$.data.bulkPrintLetterIdApp", is(randomId.toString())));
        verify(bulkPrintService, times(1)).printApplicantConsentOrderNotApprovedDocuments(any(), any());
        verify(bulkPrintService, times(1)).sendOrderForBulkPrintRespondent(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithRespondentAndApplicantAddressAsSolicitorEmailIsNo() throws Exception {
        final UUID randomId = UUID.randomUUID();
        requestContent =
                objectMapper.readTree(
                        new File(getClass()
                                .getResource(CONSENTED_BULK_PRINT_SIMPLE_JSON)
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(coverSheetService.generateApplicantCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.printApplicantConsentOrderApprovedDocuments(any(), any())).thenReturn(randomId);
        when(bulkPrintService.sendOrderForBulkPrintRespondent(any(), any())).thenReturn(randomId);

        mvc.perform(
                post(BULK_PRINT_URI)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())))
                .andExpect(jsonPath("$.data.bulkPrintLetterIdApp", is(randomId.toString())));
        verify(bulkPrintService, times(1)).printApplicantConsentOrderApprovedDocuments(any(), any());
        verify(bulkPrintService, times(1)).sendOrderForBulkPrintRespondent(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldSendForBulkPrintPackWithOnlyRespondentAddress() throws Exception {
        final UUID randomId = UUID.randomUUID();
        requestContent =
                objectMapper.readTree(
                        new File(getClass()
                                .getResource(CONSENTED_BULK_PRINT_CONSENT_ORDER_APPROVED_JSON)
                                .toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendOrderForBulkPrintRespondent(any(), any())).thenReturn(randomId);

        mvc.perform(
                post(BULK_PRINT_URI)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.bulkPrintCoverSheetRes").exists())
                .andExpect(jsonPath("$.data.bulkPrintLetterIdRes", is(randomId.toString())));
        verify(bulkPrintService, times(0)).printApplicantConsentOrderApprovedDocuments(any(), any());
        verify(bulkPrintService).sendOrderForBulkPrintRespondent(any(), any());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnCoverSheetGeneration() throws Exception {
        requestContent =
                objectMapper.readTree(
                        new File(getClass().getResource(CONTESTED_HWF_JSON).toURI()));

        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenThrow(feignError());
        mvc.perform(
                post(BULK_PRINT_URI)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
        verifyNoInteractions(bulkPrintService);
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
    }

    @Test
    public void shouldThrowExceptionOnSendForBulkPrint() throws Exception {
        requestContent =
                objectMapper.readTree(
                        new File(getClass().getResource(CONTESTED_HWF_JSON).toURI()));
        when(coverSheetService.generateRespondentCoverSheet(any(), any())).thenReturn(new CaseDocument());
        when(bulkPrintService.sendOrderForBulkPrintRespondent(any(), any())).thenThrow(feignError());
        mvc.perform(
                post(BULK_PRINT_URI)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
        verify(coverSheetService).generateRespondentCoverSheet(any(), any());
        verify(bulkPrintService).sendOrderForBulkPrintRespondent(any(), any());
    }
}
