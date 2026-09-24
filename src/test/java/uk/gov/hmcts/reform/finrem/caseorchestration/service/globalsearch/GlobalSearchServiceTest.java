package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId("FinancialRemedyMVP2")
            .data(caseDataMap)
            .build();

        when(caseManagementLocationService.getCaseLocation(caseDataMap))
            .thenReturn(CaseLocation.builder()
                .region("438850")
                .build());

        globalSearchService.setGlobalSearchDataByMap(caseDetails);

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
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId("FinancialRemedyMVP2")
            .data(caseDataMap)
            .build();
        globalSearchService.setGlobalSearchDataByMap(caseDetails);

        assertEquals("Financial Remedy", caseDataMap.get("caseNameHmctsInternal"));
        assertNull(caseDataMap.get("caseManagementCategory"));
        assertNull(caseDataMap.get("caseManagementLocation"));
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsTrueNotConsented() {

        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("ccdCaseId", "12345");
        caseDataMap.put("fullApplicantName", "Jane Doe");
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId("FinancialRemedyContested")
            .data(caseDataMap)
            .build();
        globalSearchService.setGlobalSearchDataByMap(caseDetails);

        assertNull(caseDataMap.get("caseNameHmctsInternal"));
        assertNull(caseDataMap.get("caseManagementCategory"));
        assertNull(caseDataMap.get("caseManagementLocation"));
    }
}
