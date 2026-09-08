package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CUILinkToCaseAboutToSubmitHandlerTest {

    @Mock
    private InvalidateAccessCodeService invalidateAccessCodeService;

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Test
    void shouldAssignCitizenRoleUsingLatestMergedAccessCode() {
        TestHandler handler = new TestHandler(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService);

        AccessCodeCollection oldAccessCode = accessCode(UUID.randomUUID(), "old-user", LocalDateTime.now().minusDays(1));
        AccessCodeCollection latestAccessCode = accessCode(UUID.randomUUID(), "latest-user", LocalDateTime.now());

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

        var response = handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        assertThat(response.getData().getApplicantAccessCodes()).containsExactly(oldAccessCode, latestAccessCode);
        verify(assignCaseAccessService).grantCaseRoleToUser(
            eq(Long.valueOf(CASE_ID)),
            eq("latest-user"),
            eq(CaseRole.CITIZEN_APPLICANT.getCcdCode()),
            eq(null)
        );
    }

    @Test
    void shouldThrowWhenMergedAccessCodesHaveNoCitizenUserId() {
        TestHandler handler = new TestHandler(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService);

        AccessCodeCollection mergedAccessCode = accessCode(UUID.randomUUID(), null, LocalDateTime.now());

        FinremCaseData beforeData = FinremCaseData.builder().applicantAccessCodes(List.of(mergedAccessCode)).build();
        FinremCaseData currentData = FinremCaseData.builder().applicantAccessCodes(List.of()).build();

        var callbackRequest = FinremCallbackRequestFactory.from(
            Long.valueOf(CASE_ID),
            CaseType.CONTESTED,
            EventType.LINK_APPLICANT_TO_CASE,
            currentData,
            beforeData
        );

        when(invalidateAccessCodeService.mergeForInvalidation(anyList(), anyList()))
            .thenReturn(List.of(mergedAccessCode));

        assertThrows(IllegalStateException.class, () -> handler.handle(callbackRequest, TestConstants.AUTH_TOKEN));
        verifyNoInteractions(assignCaseAccessService);
    }

    @Test
    void testCanHandle() {
        TestHandler handler = new TestHandler(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService);
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LINK_APPLICANT_TO_CASE);
    }

    private static AccessCodeCollection accessCode(UUID id, String userIdamId, LocalDateTime usedAt) {
        return AccessCodeCollection.builder()
            .id(id)
            .value(AccessCodeEntry.builder().userIdamID(userIdamId).usedAt(usedAt).build())
            .build();
    }

    private static final class TestHandler extends CUILinkToCaseAboutToSubmitHandler {
        private TestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                            InvalidateAccessCodeService invalidateAccessCodeService,
                            AssignCaseAccessService assignCaseAccessService) {
            super(finremCaseDetailsMapper, invalidateAccessCodeService, assignCaseAccessService);
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
