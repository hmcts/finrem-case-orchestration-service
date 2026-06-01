package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateContactDetailsTaskTest {

    @Mock
    private CcdService ccdService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;

    @InjectMocks
    private UpdateContactDetailsTask task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadCaseReferencesFromFile() {
        task = new UpdateContactDetailsTask(caseReferenceCsvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
        task.setSecret("DUMMY_SECRET");
        task.setCsvFile("caserefs-test-encrypted.csv");
        List<CaseReference> caseReferences = task.getCaseReferences();

        assertNotNull(caseReferences);
        assertEquals("1742295478386789", caseReferences.get(0).getCaseReference());
    }

    @Test
    void testExecuteTask() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);

        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getRespondentSolicitorEmail()).thenReturn("invalidEmail");

        task.executeTask(finremCaseDetails);

        verify(contactDetailsWrapper).setRespondentSolicitorEmail("PLEASEUPDATE@amendedbycron.com");
    }
}
