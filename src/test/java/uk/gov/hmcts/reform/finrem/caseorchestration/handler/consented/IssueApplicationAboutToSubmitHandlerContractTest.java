package uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
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

public abstract class IssueApplicationAboutToSubmitHandlerContractTest {

    protected abstract FinremCallbackHandler handler();

    protected abstract OnlineFormDocumentService onlineFormDocumentService();

    protected abstract IssueApplicationService issueApplicationService();

    protected static final String MISSING_COURT_SELECTION_ERROR = "Case cannot be issued as court selection is missing.";

    @Test
    void givenCase_whenHandled_thenGenerateMiniFormAAndCoverSheetsAndPopulateAssignToJudgeFields() {
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        // Act
        handler().handle(request, AUTH_TOKEN);

        // Verify
        verify(onlineFormDocumentService()).generateMiniFormA(AUTH_TOKEN, request.getCaseDetails());
        verifyNoMoreInteractions(onlineFormDocumentService());

        verify(issueApplicationService()).populateAssignToJudgeFields(finremCaseData);
        verify(issueApplicationService()).generateCoverSheets(request.getCaseDetails(), AUTH_TOKEN);
        verifyNoMoreInteractions(issueApplicationService());
    }

    @Test
    void givenCourtSelectionMissingFromCase_whenHandle_thenShouldAddingMissingCourtErrorResponse() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(caseData);

        doThrow(new MissingCourtException("Court selection is missing"))
            .when(issueApplicationService()).generateCoverSheets(request.getCaseDetails(), AUTH_TOKEN);

        var response = handler().handle(request, AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly(MISSING_COURT_SELECTION_ERROR);
        verifyNoInteractions(onlineFormDocumentService());
        verify(issueApplicationService(), never()).populateAssignToJudgeFields(caseData);
    }
}
