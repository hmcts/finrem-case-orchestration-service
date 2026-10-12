package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CUILinkToCaseSubmittedHandlerTest {

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @ParameterizedTest
    @CsvSource({
        "CITIZEN_APPLICANT, [APPLICANT], LINK_APPLICANT_TO_CASE",
        "CITIZEN_RESPONDENT, [RESPONDENT], LINK_RESPONDENT_TO_CASE"
    })
    void givenNotificationParty_whenBuildingSignInConfirmationCorrespondenceEvent_thenUsesExpectedParty(
        NotificationParty notificationParty,
        String expectedRoleLabel,
        EventType eventType
    ) {
        TestHandler handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            signInConfirmationCorresponder,
            correspondenceEventAuditOrchestrationService,
            notificationParty
        );

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(new FinremCaseData())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(eventType)
            .build();

        SendCorrespondenceEvent expectedEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(
            caseDetails,
            AUTH_TOKEN,
            notificationParty
        )).thenReturn(expectedEvent);

        SendCorrespondenceEvent actualEvent =
            handler.buildCorrespondenceEventForTest(
                callbackRequest,
                AUTH_TOKEN
            );

        assertThat(actualEvent).isSameAs(expectedEvent);

        verify(signInConfirmationCorresponder)
            .buildCorrespondenceEvent(
                caseDetails,
                AUTH_TOKEN,
                notificationParty
            );

        assertThat(handler.correspondenceTaskDescriptionForTest())
            .isEqualTo(
                "Send sign in confirmation email: " + expectedRoleLabel
            );
    }

    private static final class TestHandler extends CUILinkToCaseSubmittedHandler {

        private final NotificationParty notificationParty;

        private TestHandler(
            FinremCaseDetailsMapper finremCaseDetailsMapper,
            EvidenceManagementDeleteService evidenceManagementDeleteService,
            RetryExecutor retryExecutor,
            SignInConfirmationCorresponder signInConfirmationCorresponder,
            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService,
            NotificationParty notificationParty
        ) {
            super(
                finremCaseDetailsMapper,
                evidenceManagementDeleteService,
                retryExecutor,
                signInConfirmationCorresponder,
                correspondenceEventAuditOrchestrationService
            );

            this.notificationParty = notificationParty;
        }

        SendCorrespondenceEvent buildCorrespondenceEventForTest(
            FinremCallbackRequest callbackRequest,
            String userAuthorisation
        ) {
            return buildSendCorrespondenceEvent(
                callbackRequest,
                userAuthorisation
            );
        }

        String correspondenceTaskDescriptionForTest() {
            return correspondenceTaskDescription();
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }

        @Override
        protected NotificationParty notificationParty() {
            return notificationParty;
        }
    }
}
