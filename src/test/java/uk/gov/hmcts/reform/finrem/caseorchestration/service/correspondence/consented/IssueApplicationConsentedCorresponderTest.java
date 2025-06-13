package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consented;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedCorresponderTest {

    @InjectMocks
    private IssueApplicationConsentedCorresponder underTest;

    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @ParameterizedTest
    @EnumSource(value = DocumentHelper.PaperNotificationRecipient.class,
        names = {"APPLICANT", "RESPONDENT"})
    void givenConsentedCase_whenGetDocumentToPrint_thenReturnExpectedDocument(DocumentHelper.PaperNotificationRecipient party) {
        CaseDocument expectedCaseDocument = expectedCaseDocument();

        // Arrange
        FinremCaseDetails caseDetails = consentedCaseDetails();
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
            party)).thenReturn(expectedCaseDocument);
        switch (party) {
            case APPLICANT:
                lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(unexpectedCaseDocument());
                break;
            case RESPONDENT:
                lenient().when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(unexpectedCaseDocument());
                break;
        }

        // Act
        CaseDocument result = underTest.getDocumentToPrint(caseDetails, AUTH_TOKEN, party);

        // Assert
        assertThat(result).isEqualTo(expectedCaseDocument);
    }

    private FinremCaseDetails consentedCaseDetails() {
        return FinremCaseDetailsBuilderFactory.from(CASE_ID, CaseType.CONSENTED, FinremCaseData.builder().build())
            .build();
    }

    private CaseDocument expectedCaseDocument() {
        return CaseDocument.builder().documentFilename("expected").build();
    }

    private CaseDocument unexpectedCaseDocument() {
        return CaseDocument.builder().documentFilename("unexpected").build();
    }

}
