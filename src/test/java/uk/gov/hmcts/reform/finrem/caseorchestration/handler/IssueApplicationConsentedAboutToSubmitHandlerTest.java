package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ReferToJudgeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedAboutToSubmitHandlerTest {

    static LocalDate fixedLocalDate = LocalDate.of(2026, 2, 2);

    @InjectMocks
    private IssueApplicationConsentedAboutToSubmitHandler handler;

    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;

    @Mock
    private DefaultsConfiguration defaultsConfiguration;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenHandled_thenGenerateMiniFormA() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        when(defaultsConfiguration.getAssignedToJudgeDefault()).thenReturn("ASSIGNED_TO_JUDGE_DEFAULT");

        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(fixedLocalDate);
            // Act
            handler.handle(request, AUTH_TOKEN);
        }

        // Verify
        verify(onlineFormDocumentService).generateMiniFormA(AUTH_TOKEN, request.getCaseDetails());
        verifyNoMoreInteractions(onlineFormDocumentService);

        assertThat(finremCaseData)
            .extracting(
                FinremCaseData::getAssignedToJudge,
                FinremCaseData::getAssignedToJudgeReason)
            .containsExactly(
                "ASSIGNED_TO_JUDGE_DEFAULT",
                DRAFT_CONSENT_ORDER);
        assertThat(finremCaseData.getReferToJudgeWrapper())
            .extracting(
                ReferToJudgeWrapper::getReferToJudgeDate,
                ReferToJudgeWrapper::getReferToJudgeText)
            .containsExactly(
                fixedLocalDate,
                "consent for approval");
    }
}
