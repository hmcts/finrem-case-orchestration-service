package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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

        globalSearchService.setGlobalSearchData(caseData);

        verify(caseData).setCaseNameHmctsInternal("John Smith");
    }

    @Test
    public void shouldSetGlobalSearchFieldsFromMap() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap);

        assertEquals("Jane Doe", caseDataMap.get("caseNameHmctsInternal"));
    }

}
