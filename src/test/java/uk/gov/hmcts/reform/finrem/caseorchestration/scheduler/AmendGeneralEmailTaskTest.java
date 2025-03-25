package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class AmendGeneralEmailTaskTest {

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @InjectMocks
    private AmendGeneralEmailTask task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadCaseReferencesFromFile() {
        task = new AmendGeneralEmailTask(caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        task.setSecret("DUMMY_SECRET");
        List<CaseReference> caseReferences = task.getCaseReferences();
        assertNotNull(caseReferences);
    }

    @Test
    void testExecuteTask() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        GeneralEmailWrapper generalEmailWrapper = mock(GeneralEmailWrapper.class);

        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(caseData.getGeneralEmailWrapper()).thenReturn(generalEmailWrapper);
        when(generalEmailWrapper.getGeneralEmailUploadedDocument()).thenReturn(new CaseDocument());

        task.executeTask(finremCaseDetails);

        verify(generalEmailWrapper).setGeneralEmailUploadedDocument(null);
    }
}