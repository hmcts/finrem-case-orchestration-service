package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseManagementLocationService caseManagementLocationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GlobalSearchService globalSearchService;

    @Test
    void shouldSetGlobalSearchFieldsFromMap() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("bristolFRCourtList", "FR_bristolList_3");
        caseDataMap.put("respondentLname", "Doe");
        caseDataMap.put("applicantLname", "Jane");

        when(caseManagementLocationService.getCaseLocation(caseDataMap))
            .thenReturn(CaseLocation.builder()
                .region("438850")
                .build());

        globalSearchService.setGlobalSearchDataByMap(caseDataMap);

        assertEquals("Jane vs Doe", caseDataMap.get("caseNameHmctsInternal"));

        assertEquals(
            "Financial Remedy",
            ((DynamicList) caseDataMap.get("caseManagementCategory"))
                .getValue()
                .getCode()
        );

        assertEquals(
            "438850",
            ((CaseLocation) caseDataMap.get("caseManagementLocation"))
                .getRegion()
        );
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsDisabled() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(false);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap);

        assertNull(caseDataMap.get("caseNameHmctsInternal"));
        assertNull(caseDataMap.get("caseManagementCategory"));
        assertNull(caseDataMap.get("caseManagementLocation"));
    }
}
