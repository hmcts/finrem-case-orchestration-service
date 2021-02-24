package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ShareDocumentsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SHARE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SHARE_DOCUMENTS;

@WebMvcTest(ShareDocumentsController.class)
public class ShareDocumentsControllerTest extends BaseControllerTest {

    @Autowired
    private ShareDocumentsController shareDocumentsController;

    @MockBean
    private ShareDocumentsService shareDocumentsService;

    @Test
    public void whenApplicantSharesDocuments_thenRelevantServiceMethodsAreInvoked() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(APPLICANT_SHARE_DOCUMENTS, YES_VALUE);

        shareDocumentsController.shareDocumentsWithRespondent(callbackRequest);

        verify(shareDocumentsService, never()).shareDocumentsWithApplicant(any());
        verify(shareDocumentsService).shareDocumentsWithRespondent(any());
    }

    @Test
    public void whenApplicantStopsSharingDocuments_thenRelevantServiceMethodsAreInvoked() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(APPLICANT_SHARE_DOCUMENTS, NO_VALUE);

        shareDocumentsController.shareDocumentsWithRespondent(callbackRequest);

        verify(shareDocumentsService, never()).shareDocumentsWithApplicant(any());
        verify(shareDocumentsService, never()).shareDocumentsWithRespondent(any());
        verify(shareDocumentsService, never()).clearSharedDocumentsVisibleToApplicant(any());
        verify(shareDocumentsService).clearSharedDocumentsVisibleToRespondent(any());
    }

    @Test
    public void whenRespondentSharesDocuments_thenRelevantServiceMethodsAreInvoked() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(RESPONDENT_SHARE_DOCUMENTS, YES_VALUE);

        shareDocumentsController.shareDocumentsWithApplicant(callbackRequest);

        verify(shareDocumentsService).shareDocumentsWithApplicant(any());
        verify(shareDocumentsService, never()).shareDocumentsWithRespondent(any());
    }

    @Test
    public void whenRespondentStopsSharingDocuments_thenRelevantServiceMethodsAreInvoked() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(RESPONDENT_SHARE_DOCUMENTS, NO_VALUE);

        shareDocumentsController.shareDocumentsWithApplicant(callbackRequest);

        verify(shareDocumentsService, never()).shareDocumentsWithApplicant(any());
        verify(shareDocumentsService, never()).shareDocumentsWithRespondent(any());
        verify(shareDocumentsService).clearSharedDocumentsVisibleToApplicant(any());
        verify(shareDocumentsService, never()).clearSharedDocumentsVisibleToRespondent(any());
    }
}
