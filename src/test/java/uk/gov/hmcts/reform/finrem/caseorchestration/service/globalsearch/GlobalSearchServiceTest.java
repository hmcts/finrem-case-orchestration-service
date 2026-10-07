package uk.gov.hmcts.reform.finrem.caseorchestration.service.globalsearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseLocation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.caselocation.CaseManagementLocationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void shouldSetGlobalSearchFieldsForConsentedCaseWhenFeatureIsEnabled() {
        FinremCaseData caseData = createConsentedCaseData();
        CaseLocation expectedLocation = CaseLocation.builder().region("6").baseLocation("438850").build();
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        when(caseManagementLocationService.getCaseLocation(caseData)).thenReturn(expectedLocation);

        globalSearchService.setGlobalSearchDataByMap(caseData);

        assertEquals("Jane vs Doe", caseData.getCaseNameHmctsInternal());
        assertEquals("Financial Remedy", caseData.getCaseManagementCategory().getValue().getCode());
        assertEquals(expectedLocation, caseData.getCaseManagementLocation());
        verify(caseManagementLocationService).getCaseLocation(caseData);
    }

    @Test
    void shouldNotSetGlobalSearchFieldsWhenFeatureIsDisabled() {
        FinremCaseData caseData = createConsentedCaseData();
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(false);

        globalSearchService.setGlobalSearchDataByMap(caseData);

        assertNull(caseData.getCaseNameHmctsInternal());
        assertNull(caseData.getCaseManagementCategory());
        assertNull(caseData.getCaseManagementLocation());
        verifyNoInteractions(caseManagementLocationService);
    }

    @Test
    void shouldNotSetGlobalSearchFieldsForContestedCase() {
        FinremCaseData caseData = createConsentedCaseData().toBuilder()
            .ccdCaseType(CaseType.CONTESTED)
            .build();
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);

        globalSearchService.setGlobalSearchDataByMap(caseData);

        assertNull(caseData.getCaseNameHmctsInternal());
        assertNull(caseData.getCaseManagementCategory());
        assertNull(caseData.getCaseManagementLocation());
        verifyNoInteractions(caseManagementLocationService);
    }

    @Test
    void shouldUseFinancialRemedyWhenEitherNameIsMissing() {
        FinremCaseData caseData = createConsentedCaseData();
        caseData.getContactDetailsWrapper().setAppRespondentLName(null);
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        when(caseManagementLocationService.getCaseLocation(caseData)).thenReturn(null);

        globalSearchService.setGlobalSearchDataByMap(caseData);

        assertEquals("Financial Remedy", caseData.getCaseNameHmctsInternal());
        assertEquals("Financial Remedy", caseData.getCaseManagementCategory().getValue().getCode());
        assertNull(caseData.getCaseManagementLocation());
    }

    private FinremCaseData createConsentedCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId("12345")
            .ccdCaseType(CaseType.CONSENTED)
            .build();
        caseData.getContactDetailsWrapper().setApplicantLname("Jane");
        caseData.getContactDetailsWrapper().setAppRespondentLName("Doe");
        return caseData;
    }
}
