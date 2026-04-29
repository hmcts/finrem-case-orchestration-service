package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SetGeneralApplicationReferToJudgeEmailTaskTest {

    private SetGeneralApplicationReferToJudgeEmailTask task;

    @BeforeEach
    void setUp() {
        task = new SetGeneralApplicationReferToJudgeEmailTask(
            mock(CaseReferenceCsvLoader.class),
            mock(CcdService.class),
            mock(SystemUserService.class),
            mock(FinremCaseDetailsMapper.class)
        );
    }

    @Test
    void shouldUpdateIncorrectEmail() {
        // Arrange
        GeneralApplicationWrapper wrapper = new GeneralApplicationWrapper();
        wrapper.setGeneralApplicationReferToJudgeEmail("watford.@j.com");
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(wrapper)
            .build();
        FinremCaseDetails details = FinremCaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();

        // Act
        task.executeTask(details);

        // Assert
        assertEquals("watford@j.com", wrapper.getGeneralApplicationReferToJudgeEmail());
    }

    @Test
    void shouldNotUpdateIfEmailIsCorrect() {
        // Arrange
        GeneralApplicationWrapper wrapper = new GeneralApplicationWrapper();
        wrapper.setGeneralApplicationReferToJudgeEmail("test@test.com");
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(wrapper)
            .build();
        FinremCaseDetails details = FinremCaseDetails.builder()
            .id(456L)
            .data(caseData)
            .build();

        // Act
        task.executeTask(details);

        // Assert
        assertEquals("test@test.com", wrapper.getGeneralApplicationReferToJudgeEmail());
    }

    @Test
    void shouldNotUpdateIfEmailIsNull() {
        // Arrange
        GeneralApplicationWrapper wrapper = new GeneralApplicationWrapper();
        wrapper.setGeneralApplicationReferToJudgeEmail(null);
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(wrapper)
            .build();
        FinremCaseDetails details = FinremCaseDetails.builder()
            .id(789L)
            .data(caseData)
            .build();

        // Act
        task.executeTask(details);

        // Assert
        assertNull(wrapper.getGeneralApplicationReferToJudgeEmail());
    }

    @Test
    void shouldNotThrowIfWrapperIsNull() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .generalApplicationWrapper(null)
            .build();
        FinremCaseDetails details = FinremCaseDetails.builder()
            .id(101L)
            .data(caseData)
            .build();

        // Act & Assert
        assertDoesNotThrow(() -> task.executeTask(details));
    }
}
