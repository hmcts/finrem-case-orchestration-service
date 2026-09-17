package uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ReferToJudgeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;

@ExtendWith(MockitoExtension.class)
class IssueApplicationServiceTest {

    private static final LocalDate fixedLocalDate = LocalDate.of(2020, Month.JUNE, 4);

    @Mock
    private Clock clock;

    @Mock
    private GenerateCoverSheetService generateCoverSheetService;

    @Mock
    private DefaultsConfiguration defaultsConfiguration;

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

    @Test
    void shouldPopulateAssignToJudgeFields() {
        when(defaultsConfiguration.getAssignedToJudgeDefault()).thenReturn("DEFAULT");

        FinremCaseData caseData = FinremCaseData.builder().build();

        try (MockedStatic<LocalDate> localTimeMockedStatic = Mockito.mockStatic(LocalDate.class)) {
            localTimeMockedStatic.when(() -> LocalDate.now(clock)).thenReturn(fixedLocalDate);

            underTest.populateAssignToJudgeFields(caseData);
        }

        assertThat(caseData)
            .extracting(
                FinremCaseData::getAssignedToJudge,
                FinremCaseData::getAssignedToJudgeReason)
            .contains("DEFAULT",
                DRAFT_CONSENT_ORDER);
        assertThat(caseData.getReferToJudgeWrapper())
            .extracting(
                ReferToJudgeWrapper::getReferToJudgeDate,
                ReferToJudgeWrapper::getReferToJudgeText)
            .contains(LocalDate.of(2020, Month.JUNE,4), "consent for approval");
    }
}
