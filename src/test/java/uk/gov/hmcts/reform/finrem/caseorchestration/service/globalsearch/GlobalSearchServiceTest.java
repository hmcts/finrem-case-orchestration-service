package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private GlobalSearchService globalSearchService;

    @Test
    void shouldSetGlobalSearchFieldsFromMap() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap);

        assertEquals("Jane Doe", caseDataMap.get("caseNameHmctsInternal"));
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsDisabled() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(false);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap);

        assertNull(caseDataMap.get("caseNameHmctsInternal"));
    }
}
