package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SetDateOfMarriageTaskTest {

    @Mock
    private CaseReferenceCsvLoader csvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private SetDateOfMarriageTask task;

    @BeforeEach
    void setUp() {
        task = new SetDateOfMarriageTask(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Test
    void shouldUpdateDateOfMarriageWhenDateMatchesIncorrectValue() {
        FinremCaseData caseData = FinremCaseData.builder()
            .dateOfMarriage(LocalDate.of(2005, 3, 25))
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1234567890123456L)
            .data(caseData)
            .build();

        task.executeTask(caseDetails);

        assertThat(caseData.getDateOfMarriage()).isEqualTo(LocalDate.of(2005, 3, 29));
    }

    @Test
    void shouldNotUpdateDateOfMarriageWhenDateDoesNotMatchIncorrectValue() {
        LocalDate originalDate = LocalDate.of(2006, 1, 1);

        FinremCaseData caseData = FinremCaseData.builder()
            .dateOfMarriage(originalDate)
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(1234567890123456L)
            .data(caseData)
            .build();

        task.executeTask(caseDetails);

        assertThat(caseData.getDateOfMarriage()).isEqualTo(originalDate);
    }

    @Test
    void shouldReturnConfiguredCaseListFileName() {
        ReflectionTestUtils.setField(task, "csvFile", "updateDateOfMarriage-encrypted.csv");

        assertThat(task.getCaseListFileName()).isEqualTo("updateDateOfMarriage-encrypted.csv");
    }

    @Test
    void shouldReturnConfiguredTaskEnabledFlag() {
        ReflectionTestUtils.setField(task, "taskEnabled", true);

        assertThat(task.isTaskEnabled()).isTrue();
    }

    @Test
    void shouldReturnConfiguredCaseType() {
        ReflectionTestUtils.setField(task, "caseTypeId", "FinancialRemedyContested");

        assertThat(task.getCaseType()).isEqualTo(CaseType.CONTESTED);
    }

    @Test
    void shouldReturnTaskName() {
        assertThat(task.getTaskName()).isEqualTo("SetDateOfMarriageTask");
    }

    @Test
    void shouldReturnSummary() {
        assertThat(task.getSummary()).isEqualTo("DFR-5060 CT Fix Date of Marriage");
    }
}
