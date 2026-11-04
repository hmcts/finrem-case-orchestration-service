package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUIApplicantDocumentUploadSubmittedHandlerTest {

    @InjectMocks
    private CUIApplicantDocumentUploadSubmittedHandler handler;

    @Mock
    private CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(
            handler,
            CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.CUI_APPLICANT_DOCUMENT_UPLOAD
        );
    }

    @Test
    void shouldUseApplicantNotificationParty() {
        assertThat(handler.notificationParty())
            .isEqualTo(NotificationParty.CITIZEN_APPLICANT);
    }
}
