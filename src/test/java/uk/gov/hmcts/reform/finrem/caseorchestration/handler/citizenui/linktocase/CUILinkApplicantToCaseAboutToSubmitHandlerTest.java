package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AccessCodeEntry;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkApplicantToCaseAboutToSubmitHandlerTest {

    private static final String USER_ID = "citizen-user-id";
    private static final String APPLICANT_ROLE = "[APPLICANT]";

    @Mock
    private InvalidateAccessCodeService invalidateAccessCodeService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @InjectMocks
    private CUILinkApplicantToCaseAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LINK_APPLICANT_TO_CASE);
    }

    @Test
    void shouldMergeApplicantAccessCodesAndReturnUpdatedCaseData() {
        AccessCodeCollection beforeCode = accessCode(UUID.randomUUID(), "before-user-id");
        AccessCodeCollection mergedCode = accessCode(UUID.randomUUID(), USER_ID);

        FinremCaseData beforeData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(beforeCode))
            .build();

        FinremCaseData currentData = FinremCaseData.builder()
            .applicantAccessCodes(List.of())
            .build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedCode));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        assertThat(response.getData().getApplicantAccessCodes())
            .containsExactly(mergedCode);

        verify(invalidateAccessCodeService).mergeForInvalidation(
            List.of(beforeCode),
            List.of()
        );
        verify(assignCaseAccessService).grantCaseRoleToUser(eq(Long.valueOf(CASE_ID)), eq(USER_ID), eq(APPLICANT_ROLE), eq(null));
    }

    @Test
    void shouldHandleNullAccessCodeListsGracefully() {
        FinremCaseData beforeData = FinremCaseData.builder().build();
        FinremCaseData currentData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(accessCode(UUID.randomUUID(), USER_ID)))
            .build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(accessCode(UUID.randomUUID(), USER_ID)));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        assertThat(response.getData().getApplicantAccessCodes()).hasSize(1);
        verify(assignCaseAccessService).grantCaseRoleToUser(eq(Long.valueOf(CASE_ID)), eq(USER_ID), eq(APPLICANT_ROLE), eq(null));
    }

    @Test
    void shouldAssignRoleUsingLatestAccessCodeUserId() {
        String oldUserId = "old-user-id";
        String latestUserId = "latest-user-id";

        AccessCodeCollection oldAccessCode = accessCode(UUID.randomUUID(), oldUserId, LocalDateTime.now().minusDays(1));
        AccessCodeCollection latestAccessCode = accessCode(UUID.randomUUID(), latestUserId, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder().applicantAccessCodes(List.of(oldAccessCode, latestAccessCode)).build();
        FinremCaseData currentData = FinremCaseData.builder().applicantAccessCodes(List.of()).build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(oldAccessCode, latestAccessCode));

        handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        verify(assignCaseAccessService).grantCaseRoleToUser(eq(Long.valueOf(CASE_ID)), eq(latestUserId), eq(APPLICANT_ROLE), eq(null));
    }

    private AccessCodeCollection accessCode(UUID id, String userIdamId) {
        return accessCode(id, userIdamId, null);
    }

    private AccessCodeCollection accessCode(UUID id, String userIdamId, LocalDateTime usedAt) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(AccessCodeEntry.builder().userIdamID(userIdamId).usedAt(usedAt).build())
            .build();
    }
}
