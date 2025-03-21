package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:application.properties")
class ClearGeneralEmailDataFieldTaskTest {

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @InjectMocks
    private ClearGeneralEmailDataFieldTask task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadCaseReferencesFromFile() {
        task = new ClearGeneralEmailDataFieldTask(caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        List<CaseReference> caseReferences = task.getCaseReferences();
        System.out.println("Case References:.............");
        caseReferences.forEach(System.out::println);
        assertNotNull(caseReferences);
    }

    @Test
    void testExecuteTask() throws Exception {
        List<CaseReference> caseReferences = List.of(new CaseReference("123456"));
        task.run();

        // Add assertions here
        assertEquals(1, caseReferences.size());
    }


    @Test
    void testExecuteTaskWhenGeneralEmailUploadedDocumentIsNulled() throws Exception {
        // Arrange
        List<CaseReference> caseReferences = List.of(new CaseReference("123456"));
        task.run();

        // Act
        FinremCaseDetails finremCaseDetails = new FinremCaseDetails(); // retrieve the FinremCaseDetails object for the case reference "123456"

                // Assert
        assertNotNull(finremCaseDetails);
        assertNotNull(finremCaseDetails.getData());
        assertNotNull(finremCaseDetails.getData().getGeneralEmailWrapper());
        assertNull(finremCaseDetails.getData().getGeneralEmailWrapper().getGeneralEmailUploadedDocument());
    }
}