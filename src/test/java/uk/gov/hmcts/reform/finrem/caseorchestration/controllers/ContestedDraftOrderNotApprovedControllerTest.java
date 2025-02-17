package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDataWithRefusalOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;

@WebMvcTest(ContestedDraftOrderNotApprovedController.class)
public class ContestedDraftOrderNotApprovedControllerTest extends BaseControllerTest {

    @MockitoBean
    private ContestedDraftOrderNotApprovedService contestedDraftOrderNotApprovedService;
    @MockitoBean
    private BulkPrintService bulkPrintService;
    @MockitoBean
    private PaperNotificationService paperNotificationService;
    @MockitoBean
    private IdamService idamService;
    @MockitoBean
    private DocumentHelper documentHelper;

    private static final String START_REFUSAL_ORDER_URL = "/case-orchestration/contested-application-not-approved-start";
    private static final String PREVIEW_REFUSAL_ORDER_URL = "/case-orchestration/documents/preview-refusal-order";
    private static final String SUBMIT_REFUSAL_ORDER_URL = "/case-orchestration/contested-application-not-approved-submit";
    private static final String SUBMIT_REFUSAL_REASON_URL = "/case-orchestration/contested-application-send-refusal";
    private static final String BEARER_TOKEN = "some-access-token";

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
        whenServiceGeneratesDocument().thenReturn(caseDataWithRefusalOrder());

        mvc.perform(post(PREVIEW_REFUSAL_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.refusalOrderPreviewDocument.document_url", is(DOC_URL)))
            .andExpect(
                jsonPath("$.data.refusalOrderPreviewDocument.document_filename",
                    is(FILE_NAME)))
            .andExpect(
                jsonPath("$.data.refusalOrderPreviewDocument.document_binary_url",
                    is(BINARY_URL)));
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
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(PREVIEW_REFUSAL_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private OngoingStubbing<Map<String, Object>> whenServiceGeneratesDocument() {
        return when(contestedDraftOrderNotApprovedService.createRefusalOrder(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    @Test
    public void submitRefusalOrderSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServicePopulatesCollection().thenReturn(caseDataWithRefusalOrder());

        mvc.perform(post(SUBMIT_REFUSAL_ORDER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).populateRefusalOrderCollection(any(CaseDetails.class));
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
        when(contestedDraftOrderNotApprovedService.getLatestRefusalReason(any())).thenReturn(Optional.of(caseDocument()));
        when(paperNotificationService.shouldPrintForApplicant(any(CaseDetails.class))).thenReturn(true);
        when(paperNotificationService.shouldPrintForRespondent(any(CaseDetails.class))).thenReturn(true);
        mvc.perform(post(SUBMIT_REFUSAL_REASON_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).getLatestRefusalReason(any());
        verify(bulkPrintService).printApplicantDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService).printRespondentDocuments(any(CaseDetails.class), any(), any());
    }

    @Test
    public void submitSendRefusalReasonWithRefusalAndShouldNotPrintForParties() throws Exception {
        doValidRefusalOrder();
        when(contestedDraftOrderNotApprovedService.getLatestRefusalReason(any())).thenReturn(Optional.of(caseDocument()));
        when(paperNotificationService.shouldPrintForApplicant(any(CaseDetails.class))).thenReturn(false);
        when(paperNotificationService.shouldPrintForRespondent(any(CaseDetails.class))).thenReturn(false);
        mvc.perform(post(SUBMIT_REFUSAL_REASON_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(contestedDraftOrderNotApprovedService).getLatestRefusalReason(any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(CaseDetails.class), any(), any());
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
        verify(bulkPrintService, never()).printRespondentDocuments(any(CaseDetails.class), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments(any(CaseDetails.class), any(), any());
    }

    private OngoingStubbing<Map<String, Object>> whenServicePopulatesCollection() {
        return when(contestedDraftOrderNotApprovedService.populateRefusalOrderCollection(isA(CaseDetails.class)));
    }
}
