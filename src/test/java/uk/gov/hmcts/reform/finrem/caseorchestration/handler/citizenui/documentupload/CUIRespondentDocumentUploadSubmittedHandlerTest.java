package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload.CUIDocumentUploadSubmittedHandlerTest.callbackRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload.CUIDocumentUploadSubmittedHandlerTest.caseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUIRespondentDocumentUploadSubmittedHandlerTest {

    @InjectMocks
    private CUIRespondentDocumentUploadSubmittedHandler handler;

    @Mock
    private CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD);
    }

    @Test
    void shouldUseRespondentNotificationParty() {
        FinremCaseDetails details = caseDetails();
        FinremCallbackRequest request = callbackRequest(details, EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD);

        when(citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(any(), anyString(), any()))
            .thenReturn(SendCorrespondenceEvent.builder().caseDetails(details).build());
        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString())).thenReturn(true);

        handler.handle(request, TestConstants.AUTH_TOKEN);

        verify(citizenDocumentsUploadedCorresponder)
            .buildCorrespondenceEvent(details, TestConstants.AUTH_TOKEN, NotificationParty.CITIZEN_RESPONDENT);
    }
}
