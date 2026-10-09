package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CITIZEN_IDAM_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkToCaseAboutToSubmitHandlerTest {

    @Mock
    private InvalidateAccessCodeService invalidateAccessCodeService;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private SignInConfirmationCorresponder signInConfirmationCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private TestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestHandler(
            finremCaseDetailsMapper,
            invalidateAccessCodeService,
            assignCaseAccessService,
            signInConfirmationCorresponder,
            correspondenceEventAuditOrchestrationService
        );
    }

    @Test
    void shouldAssignCitizenRoleUsingLatestMergedAccessCodeAndCreatePendingNotificationAudit() {
        AccessCodeCollection oldAccessCode = accessCode(UUID.randomUUID(), null, null);
        AccessCodeCollection latestAccessCode = accessCode(UUID.randomUUID(), CITIZEN_IDAM_USER_ID, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder().applicantAccessCodes(List.of(oldAccessCode, latestAccessCode)).build();
        FinremCaseData currentData = FinremCaseData.builder().applicantAccessCodes(List.of()).build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        List<AccessCodeCollection> mergedAccessCodes = List.of(oldAccessCode, latestAccessCode);

        SendCorrespondenceEvent correspondenceEvent =
            SendCorrespondenceEvent.builder()
                .caseDetails(callbackRequest.getCaseDetails())
                .build();

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(mergedAccessCodes);

        when(signInConfirmationCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            AUTH_TOKEN,
            NotificationParty.CITIZEN_APPLICANT
        )).thenReturn(correspondenceEvent);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getApplicantAccessCodes())
            .containsExactly(oldAccessCode, latestAccessCode);

        verify(assignCaseAccessService).grantCaseRoleToUser(
            Long.valueOf(CASE_ID),
            CITIZEN_IDAM_USER_ID,
            CaseRole.CITIZEN_APPLICANT.getCcdCode(),
            null
        );

        verify(signInConfirmationCorresponder).buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            AUTH_TOKEN,
            NotificationParty.CITIZEN_APPLICANT
        );

        verify(correspondenceEventAuditOrchestrationService).createPendingAudits(
            correspondenceEvent,
            EventType.LINK_APPLICANT_TO_CASE
        );
    }

    @Test
    void shouldThrowWhenMergedAccessCodesHaveNoCitizenUserIdAndNotCreateNotificationAudit() {
        AccessCodeCollection mergedAccessCode = accessCode(UUID.randomUUID(), null, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder().applicantAccessCodes(List.of(mergedAccessCode)).build();
        FinremCaseData currentData = FinremCaseData.builder().applicantAccessCodes(List.of()).build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedAccessCode));

        assertThrows(
            IllegalStateException.class,
            () -> handler.handle(callbackRequest, AUTH_TOKEN)
        );

        verifyNoInteractions(assignCaseAccessService);
        verifyNoInteractions(signInConfirmationCorresponder);
        verifyNoInteractions(correspondenceEventAuditOrchestrationService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(
            handler,
            CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE
        );
    }

    public static AccessCodeCollection accessCode(UUID id, String userIdamId, LocalDateTime usedAt) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(AccessCodeEntry.builder().userIdamID(userIdamId).usedAt(usedAt).build())
            .build();
    }

    private static final class TestHandler extends CUILinkToCaseAboutToSubmitHandler {

        private TestHandler(
            FinremCaseDetailsMapper finremCaseDetailsMapper,
            InvalidateAccessCodeService invalidateAccessCodeService,
            AssignCaseAccessService assignCaseAccessService,
            SignInConfirmationCorresponder signInConfirmationCorresponder,
            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
            super(
                finremCaseDetailsMapper,
                invalidateAccessCodeService,
                assignCaseAccessService,
                signInConfirmationCorresponder,
                correspondenceEventAuditOrchestrationService
            );
        }

        @Override
        protected NotificationParty notificationParty() {
            return NotificationParty.CITIZEN_APPLICANT;
        }

        @Override
        protected EventType handledEventType() {
            return EventType.LINK_APPLICANT_TO_CASE;
        }

        @Override
        protected String citizenCaseRole() {
            return CaseRole.CITIZEN_APPLICANT.getCcdCode();
        }

        @Override
        protected List<AccessCodeCollection> getAccessCodes(FinremCaseData data) {
            return data.getApplicantAccessCodes();
        }

        @Override
        protected void setAccessCodes(FinremCaseData data, List<AccessCodeCollection> accessCodes) {
            data.setApplicantAccessCodes(accessCodes);
        }
    }
}
