package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

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

    @InjectMocks
    private GlobalSearchService globalSearchService;

    @Test
    void shouldSetGlobalSearchFieldsFromMap() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("bristolFRCourtList", "FR_bristolList_3");
        caseDataMap.put("appRespondentLName", "Doe");
        caseDataMap.put("applicantLName", "Jane");

        when(caseManagementLocationService.getCaseLocation(caseDataMap))
            .thenReturn(CaseLocation.builder()
                .region("438850")
                .build());

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyMVP2", 1323222L);

        assertEquals("Jane vs Doe", caseDataMap.get("caseNameHmctsInternal"));

        assertEquals("Financial Remedy", ((DynamicList) caseDataMap.get("caseManagementCategory"))
                .getValue()
                .getCode()
        );

        assertEquals("438850", ((CaseLocation) caseDataMap.get("caseManagementLocation")).getRegion()
        );
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsDisabled() {

        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(false);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyMVP2", 1323222L);

        assertNull(caseDataMap.get("caseNameHmctsInternal"));
        assertNull(caseDataMap.get("caseManagementCategory"));
        assertNull(caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsTrueNotConsented() {

        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyContested", 1323222L);

        assertNull(caseDataMap.get("caseNameHmctsInternal"));
        assertNull(caseDataMap.get("caseManagementCategory"));
        assertNull(caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldNotOverwriteExistingGlobalSearchFields() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        DynamicList existingCategory = DynamicList.builder().build();
        CaseLocation existingLocation = CaseLocation.builder().region("existing-region").build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantLName", "Jane");
        caseDataMap.put("appRespondentLName", "Doe");

        caseDataMap.put("caseNameHmctsInternal", "Existing Case Name");
        caseDataMap.put("caseManagementCategory", existingCategory);
        caseDataMap.put("caseManagementLocation", existingLocation);

        globalSearchService.setGlobalSearchDataByMap(
            caseDataMap,
            "FinancialRemedyMVP2",
            1323222L
        );

        assertEquals("Existing Case Name", caseDataMap.get("caseNameHmctsInternal"));
        assertEquals(existingCategory, caseDataMap.get("caseManagementCategory"));
        assertEquals(existingLocation, caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldSetFinancialRemedyWhenNamesAreMissing() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("bristolFRCourtList", "FR_bristolList_3");
        when(caseManagementLocationService.getCaseLocation(caseDataMap))
            .thenReturn(CaseLocation.builder()
                .region("438850")
                .build());

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyMVP2", 1323222L);

        assertEquals("Financial Remedy", caseDataMap.get("caseNameHmctsInternal"));
    }

    @Test
    void shouldPopulateFieldsWhenExistingValuesAreNull() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantLName", "Jane");
        caseDataMap.put("appRespondentLName", "Doe");

        caseDataMap.put("caseNameHmctsInternal", null);
        caseDataMap.put("caseManagementCategory", null);
        caseDataMap.put("caseManagementLocation", null);

        when(caseManagementLocationService.getCaseLocation(caseDataMap)).thenReturn(CaseLocation.builder()
                .region("438850")
                .build());

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyMVP2", 1323222L);

        assertEquals("Jane vs Doe", caseDataMap.get("caseNameHmctsInternal"));
        assertEquals("438850", ((CaseLocation) caseDataMap.get("caseManagementLocation")).getRegion());
    }

    @Test
    void shouldSetNullLocationWhenLocationServiceReturnsNull() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantLName", "Jane");
        caseDataMap.put("appRespondentLName", "Doe");

        when(caseManagementLocationService.getCaseLocation(caseDataMap)).thenReturn(null);

        globalSearchService.setGlobalSearchDataByMap(caseDataMap, "FinancialRemedyMVP2", 1323222L);

        assertEquals("Jane vs Doe", caseDataMap.get("caseNameHmctsInternal"));

        assertNull(caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldNotOverwriteExistingLocation() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        CaseLocation existingLocation = CaseLocation.builder()
            .region("existing")
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caseManagementLocation", existingLocation);

        globalSearchService.setGlobalSearchDataByMap(
            caseDataMap,
            "FinancialRemedyMVP2",
            1323222L
        );

        assertEquals(existingLocation, caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldNotSetCaseManagementLocationWhenLocationServiceReturnsNull() {
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantLName", "Jane");
        caseDataMap.put("appRespondentLName", "Doe");

        when(caseManagementLocationService.getCaseLocation(caseDataMap)).thenReturn(null);

        globalSearchService.setGlobalSearchDataByMap(
            caseDataMap,
            "FinancialRemedyMVP2",
            1323222L
        );

        assertEquals("Jane vs Doe", caseDataMap.get("caseNameHmctsInternal"));

        assertEquals("Financial Remedy",
            ((DynamicList) caseDataMap.get("caseManagementCategory"))
                .getValue()
                .getCode());

        assertNull(caseDataMap.get("caseManagementLocation"));
    }
}
