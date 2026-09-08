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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InvalidateAccessCodeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    void shouldUseApplicantAccessCodesAndAssignApplicantRole() {
        AccessCodeCollection beforeApplicantCode = accessCode(UUID.randomUUID(), "before-user-id", null);
        AccessCodeCollection currentApplicantCode = accessCode(UUID.randomUUID(), "current-user-id", null);
        AccessCodeCollection mergedApplicantCode = accessCode(UUID.randomUUID(), USER_ID, LocalDateTime.now());

        AccessCodeCollection beforeRespondentCode = accessCode(UUID.randomUUID(), "resp-user", null);
        AccessCodeCollection currentRespondentCode = accessCode(UUID.randomUUID(), "resp-user", null);

        FinremCaseData beforeData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(beforeApplicantCode))
            .respondentAccessCodes(List.of(beforeRespondentCode))
            .build();

        FinremCaseData currentData = FinremCaseData.builder()
            .applicantAccessCodes(List.of(currentApplicantCode))
            .respondentAccessCodes(List.of(currentRespondentCode))
            .build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedApplicantCode));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        assertThat(response.getData().getApplicantAccessCodes())
            .containsExactly(mergedApplicantCode);
        assertThat(response.getData().getRespondentAccessCodes())
            .containsExactly(currentRespondentCode);

        verify(invalidateAccessCodeService).mergeForInvalidation(
            List.of(beforeApplicantCode),
            List.of(currentApplicantCode)
        );
        verify(assignCaseAccessService).grantCaseRoleToUser(
            eq(Long.valueOf(CASE_ID)),
            eq(USER_ID),
            eq(CaseRole.CITIZEN_APPLICANT.getCcdCode()),
            eq(null)
        );
    }

    private AccessCodeCollection accessCode(UUID id, String userIdamId, LocalDateTime usedAt) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(AccessCodeEntry.builder().userIdamID(userIdamId).usedAt(usedAt).build())
            .build();
    }
}
