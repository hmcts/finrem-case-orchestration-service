package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CITIZEN_IDAM_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase.CUILinkToCaseSubmittedHandlerTest.accessCode;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkRespondentToCaseAboutToSubmitHandlerTest {

    @Mock
    private InvalidateAccessCodeService invalidateAccessCodeService;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;
    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @InjectMocks
    private CUILinkRespondentToCaseAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LINK_RESPONDENT_TO_CASE);
    }

    @Test
    void shouldHandleLinkingRespondentToCase() {
        AccessCodeCollection beforeAccessCodes =
            accessCode(UUID.randomUUID(), null, null);
        AccessCodeCollection currentAccessCodes =
            accessCode(UUID.randomUUID(), CITIZEN_IDAM_USER_ID, null);
        AccessCodeCollection mergedAccessCodes =
            accessCode(UUID.randomUUID(), CITIZEN_IDAM_USER_ID, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder()
            .respondentAccessCodes(List.of(beforeAccessCodes))
            .build();

        FinremCaseData currentData = FinremCaseData.builder()
            .respondentAccessCodes(List.of(currentAccessCodes))
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_RESPONDENT_TO_CASE,
            currentData,
            beforeData
        );

        SendCorrespondenceEvent correspondenceEvent =
            SendCorrespondenceEvent.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .build();

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedAccessCodes));

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(callbackRequest.getCaseDetails(), AUTH_TOKEN,
            NotificationParty.CITIZEN_RESPONDENT
        )).thenReturn(correspondenceEvent);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(invalidateAccessCodeService).mergeForInvalidation(
            List.of(beforeAccessCodes),
            List.of(currentAccessCodes)
        );
        verify(assignCaseAccessService).grantCaseRoleToUser(
            Long.valueOf(CASE_ID),
            CITIZEN_IDAM_USER_ID,
            CaseRole.CITIZEN_RESPONDENT.getCcdCode(),
            null
        );
        verify(signInConfirmationCorresponder).buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            AUTH_TOKEN,
            NotificationParty.CITIZEN_RESPONDENT
        );
        verify(correspondenceEventAuditOrchestrationService).createPendingAudits(
            correspondenceEvent,
            EventType.LINK_RESPONDENT_TO_CASE
        );
    }
}
