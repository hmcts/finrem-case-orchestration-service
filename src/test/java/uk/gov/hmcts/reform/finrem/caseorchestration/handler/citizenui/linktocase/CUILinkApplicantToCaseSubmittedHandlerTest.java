package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase.CUILinkToCaseSubmittedHandlerTest.callbackRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase.CUILinkToCaseSubmittedHandlerTest.caseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkApplicantToCaseSubmittedHandlerTest {

    @InjectMocks
    private CUILinkApplicantToCaseSubmittedHandler handler;

    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LINK_APPLICANT_TO_CASE);
    }

    @Test
    void shouldUseApplicantNotificationParty() {
        FinremCaseDetails details = caseDetails();
        FinremCallbackRequest request = callbackRequest(details, EventType.LINK_APPLICANT_TO_CASE);

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(any(), anyString(), any()))
            .thenReturn(SendCorrespondenceEvent.builder().caseDetails(details).build());
        when(correspondenceEventAuditOrchestrationService.publishEvent(any(), anyString())).thenReturn(true);

        handler.handle(request, TestConstants.AUTH_TOKEN);

        verify(signInConfirmationCorresponder)
            .buildCorrespondenceEvent(details, TestConstants.AUTH_TOKEN, NotificationParty.CITIZEN_APPLICANT);
    }
}
