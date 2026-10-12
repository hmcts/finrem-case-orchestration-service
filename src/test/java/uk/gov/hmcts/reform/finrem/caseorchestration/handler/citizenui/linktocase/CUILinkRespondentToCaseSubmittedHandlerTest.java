package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkRespondentToCaseSubmittedHandlerTest {

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @InjectMocks
    private CUILinkRespondentToCaseSubmittedHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(
            handler,
            CallbackType.SUBMITTED,
            CaseType.CONTESTED,
            EventType.LINK_RESPONDENT_TO_CASE
        );
    }

    @Test
    void shouldUseRespondentNotificationParty() {
        assertThat(handler.notificationParty())
            .isEqualTo(NotificationParty.CITIZEN_RESPONDENT);
    }
}
