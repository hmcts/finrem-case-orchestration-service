package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDataWithRefusalOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;

@WebMvcTest(ContestedDraftOrderNotApprovedController.class)
public class ContestedDraftOrderNotApprovedControllerTest extends BaseControllerTest {

    @MockBean private ContestedDraftOrderNotApprovedService contestedDraftOrderNotApprovedService;
    @MockBean private BulkPrintService bulkPrintService;
    @MockBean private PaperNotificationService paperNotificationService;
    @MockBean private IdamService idamService;
    @MockBean private DocumentHelper documentHelper;

    private static final String START_REFUSAL_ORDER_URL = "/case-orchestration/contested-application-not-approved-start";
    private static final String PREVIEW_REFUSAL_ORDER_URL = "/case-orchestration/documents/preview-refusal-order";
    private static final String SUBMIT_REFUSAL_ORDER_URL = "/case-orchestration/contested-application-not-approved-submit";
    private static final String SUBMIT_REFUSAL_REASON_URL = "/case-orchestration/contested-application-send-refusal";
    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqc2NyMGE0M3JnMHU5aGZpNHRva21vdHJ"
        + "vOSIsInN1YiI6IjEiLCJpYXQiOjE1NjAyNDcyNzgsImV4cCI6MTU2MDI2NTI3OCwiZGF0YSI6ImNjZC1pbXBv"
        + "cnQsY2NkLWltcG9ydC1sb2EwIiwidHlwZSI6IkFDQ0VTUyIsImlkIjoiMSIsImZvcmVuYW1lIjoiSW50ZWdyY"
        + "XRpb24iLCJzdXJuYW1lIjoiVGVzdCIsImRlZmF1bHQtc2VydmljZSI6IlByb2JhdGUiLCJsb2EiOjAsImRlZm"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvcHJvYmF0ZSIsImdyb3VwIjoicHJvYmF0ZS1"
        + "wcml2YXRlLWJldGEifQ.sSeejKgphDGyKyNtw---nkFk5N_9iqWb2WYNHCiVRPY";

    @Test
    public void startRefusalOrderPropertiesSuccess() throws Exception {
        refusalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(BEARER_TOKEN)).thenReturn("User Name");

        mvc.perform(post(START_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_DATE, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME, is("User Name")));
    }

    @Test
    public void startRefusalOrderPropertiesBadRequest() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(START_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void startRefusalOrderPropertiesInternalServerError() throws Exception {
        refusalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(BEARER_TOKEN))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(post(START_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void refusalOrderStartControllerSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/refusal-order-contested.json").toURI()));
    }

    @Test
    public void previewRefusalOrderSuccess() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(PREVIEW_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService,
            times(1)).createAndSetRefusalOrderPreviewDocument(eq(AUTH_TOKEN),
            isA(FinremCaseDetails.class));
    }

    @Test
    public void previewRefusalOrder400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(PREVIEW_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void previewRefusalOrder500Error() throws Exception {
        doValidCaseDataSetUp();
        doThrow(feignError()).when(contestedDraftOrderNotApprovedService)
            .createAndSetRefusalOrderPreviewDocument(eq(AUTH_TOKEN), isA(FinremCaseDetails.class));

        mvc.perform(post(PREVIEW_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void submitRefusalOrderSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServicePopulatesCollection().thenReturn(finremCaseDataWithRefusalOrder());

        mvc.perform(post(SUBMIT_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).populateRefusalOrderCollection(any(FinremCaseDetails.class));
    }

    @Test
    public void submitRefusalOrder400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(SUBMIT_REFUSAL_ORDER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void submitSendRefusalReasonWithRefusalAndShouldPrintForApplicantTrue() throws Exception {
        doValidRefusalOrder();
        when(contestedDraftOrderNotApprovedService.getLatestRefusalReason(any())).thenReturn(Optional.of(newDocument()));
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(true);
        mvc.perform(post(SUBMIT_REFUSAL_REASON_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).getLatestRefusalReason(any());
        verify(bulkPrintService).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void submitSendRefusalReasonWithRefusalAndShouldNotPrintForParties() throws Exception {
        doValidRefusalOrder();
        when(contestedDraftOrderNotApprovedService.getLatestRefusalReason(any())).thenReturn(Optional.of(newDocument()));
        when(paperNotificationService.shouldPrintForApplicant(any())).thenReturn(false);
        when(paperNotificationService.shouldPrintForRespondent(any())).thenReturn(false);
        mvc.perform(post(SUBMIT_REFUSAL_REASON_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).getLatestRefusalReason(any());
        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(), any(), any());
    }

    @Test
    public void submitSendRefusalReasonWithNotRefusalReasonNotPrint() throws Exception {
        doValidCaseDataSetUpForPaperApplication();
        mvc.perform(post(SUBMIT_REFUSAL_REASON_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).getLatestRefusalReason(any());
        verify(bulkPrintService, never()).printApplicantDocuments(any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(), any(), any());
    }

    private OngoingStubbing<FinremCaseData> whenServicePopulatesCollection() {
        return when(contestedDraftOrderNotApprovedService.populateRefusalOrderCollection(isA(FinremCaseDetails.class)));
    }
}
