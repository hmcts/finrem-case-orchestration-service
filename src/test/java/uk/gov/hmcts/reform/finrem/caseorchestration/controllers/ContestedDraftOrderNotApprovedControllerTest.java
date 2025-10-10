package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
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

    private static final String PREVIEW_REFUSAL_ORDER_URL = "/case-orchestration/documents/preview-refusal-order";
    private static final String SUBMIT_REFUSAL_ORDER_URL = "/case-orchestration/contested-application-not-approved-submit";
    private static final String SUBMIT_REFUSAL_REASON_URL = "/case-orchestration/contested-application-send-refusal";

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
