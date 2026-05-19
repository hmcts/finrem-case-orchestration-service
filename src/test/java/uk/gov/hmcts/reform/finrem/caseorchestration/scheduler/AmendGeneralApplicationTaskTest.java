package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class AmendGeneralApplicationTaskTest {
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void testExecuteTask() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        GeneralApplicationWrapper generalApplicationWrapper = mock(GeneralApplicationWrapper.class);

        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(caseData.getGeneralApplicationWrapper()).thenReturn(generalApplicationWrapper);
        when(generalApplicationWrapper.getGeneralApplicationReferToJudgeEmail()).thenReturn("judge@example.com");

        AmendGeneralApplicationTask task = new AmendGeneralApplicationTask(
            caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        task.executeTask(finremCaseDetails);

        verify(generalApplicationWrapper).setGeneralApplicationReferToJudgeEmail(null);
    }

    @Test
    void testExecuteTaskWhenJudgeEmailNotSet() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        GeneralApplicationWrapper generalApplicationWrapper = mock(GeneralApplicationWrapper.class);

        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(caseData.getGeneralApplicationWrapper()).thenReturn(generalApplicationWrapper);
        when(generalApplicationWrapper.getGeneralApplicationReferToJudgeEmail()).thenReturn(null);

        AmendGeneralApplicationTask task = new AmendGeneralApplicationTask(
            caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        task.executeTask(finremCaseDetails);

        verify(generalApplicationWrapper, never()).setGeneralApplicationReferToJudgeEmail(null);
    }
}
