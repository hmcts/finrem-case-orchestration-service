package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedAboutToSubmitHandlerTest {

    @InjectMocks
    private IssueApplicationConsentedAboutToSubmitHandler handler;
    @Mock
    private OnlineFormDocumentService onlineFormDocumentService;
    @Mock
    private IssueApplicationService issueApplicationService;

    private static final String MISSING_COURT_SELECTION_ERROR = "Case cannot be issued as court selection is missing.";

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.HWF_ACCEPTED_AND_ISSUE)
        );
    }

    @Test
    void givenCase_whenHandled_thenGenerateMiniFormAAndCoverSheetsAndPopulateAssignToJudgeFields() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        // Act
        handler.handle(request, AUTH_TOKEN);

        // Verify
        verify(onlineFormDocumentService).generateMiniFormA(AUTH_TOKEN, request.getCaseDetails());
        verifyNoMoreInteractions(onlineFormDocumentService);

        verify(issueApplicationService).populateAssignToJudgeFields(finremCaseData);
        verify(issueApplicationService).generateCoverSheets(request.getCaseDetails(), AUTH_TOKEN);
        verifyNoMoreInteractions(issueApplicationService);
    }

    @Test
    void givenCourtSelectionMissingFromCase_whenHandle_thenShouldAddingMissingCourtErrorResponse() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        doThrow(new MissingCourtException("Court selection is missing"))
            .when(issueApplicationService).generateCoverSheets(request.getCaseDetails(), AUTH_TOKEN);

        var response = handler.handle(request, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly(MISSING_COURT_SELECTION_ERROR);
        verifyNoInteractions(onlineFormDocumentService);
        verify(issueApplicationService, never()).populateAssignToJudgeFields(caseData);
    }
}
