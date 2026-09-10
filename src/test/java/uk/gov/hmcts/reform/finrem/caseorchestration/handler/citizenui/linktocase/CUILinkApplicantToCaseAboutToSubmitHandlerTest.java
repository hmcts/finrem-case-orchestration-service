package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase.CUILinkToCaseAboutToSubmitHandlerTest.CITIZEN_IDAM_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase.CUILinkToCaseAboutToSubmitHandlerTest.accessCode;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkApplicantToCaseAboutToSubmitHandlerTest {

    @Mock
    private InvalidateAccessCodeService invalidateAccessCodeService;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @InjectMocks
    private CUILinkApplicantToCaseAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LINK_APPLICANT_TO_CASE);
    }

    @Test
    void shouldHandleLinkingApplicantToCase() {
        AccessCodeCollection beforeAccessCodes =
            accessCode(UUID.randomUUID(), null, null);
        AccessCodeCollection currentAccessCodes =
            accessCode(UUID.randomUUID(), CITIZEN_IDAM_USER_ID, null);
        AccessCodeCollection mergedAccessCodes =
            accessCode(UUID.randomUUID(), CITIZEN_IDAM_USER_ID, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(beforeAccessCodes))
            .build();

        FinremCaseData currentData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(currentAccessCodes))
            .build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedAccessCodes));

        handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        verify(invalidateAccessCodeService).mergeForInvalidation(
            List.of(beforeAccessCodes),
            List.of(currentAccessCodes)
        );

        verify(assignCaseAccessService).grantCaseRoleToUser(
            Long.valueOf(CASE_ID),
            CITIZEN_IDAM_USER_ID,
            CaseRole.CITIZEN_APPLICANT.getCcdCode(),
            null
        );
    }
}
