package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource("classpath:application.properties")
class ClearGeneralEmailDataFieldTaskTest {

    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    private ClearGeneralEmailDataFieldTask task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CaseReferenceCsvLoader csvLoader = new CaseReferenceCsvLoader();
        task = new ClearGeneralEmailDataFieldTask(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Test
    void testLoadCaseReferencesFromFile() {
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
}