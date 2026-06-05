package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalSearchServiceTest {
    @Mock
    private FinremCaseData caseData;

    @InjectMocks
    GlobalSearchService globalSearchService;

    @Test
    public void shouldSetGlobalSearchFields() {
        when(caseData.getCcdCaseId()).thenReturn(String.valueOf(12345L));
        when(caseData.getFullApplicantName()).thenReturn("John Smith");
        when(caseData.getSelectedAllocatedCourt()).thenReturn("London Court");

        globalSearchService.setGlobalSearchData(caseData);

        verify(caseData).setCaseNameHmctsInternal("John Smith");
        verify(caseData).setCaseManagementLocation("London Court");
    }

}