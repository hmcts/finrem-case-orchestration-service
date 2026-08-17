package uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class IssueApplicationServiceTest {

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @InjectMocks
    private IssueApplicationService underTest;

    @Test
    void shouldGenerateApplicantCoverSheet() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        underTest.generateCoverSheets(finremCaseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetApplicantCoverSheet(finremCaseDetails, AUTH_TOKEN);
    }

    @Test
    void shouldGenerateRespondentCoverSheet() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        underTest.generateCoverSheets(finremCaseDetails, AUTH_TOKEN);
        verify(generateCoverSheetService).generateAndSetRespondentCoverSheet(finremCaseDetails, AUTH_TOKEN);
    }
}
