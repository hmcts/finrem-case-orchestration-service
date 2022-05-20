package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class CheckApplicantSolicitorIsDigitalServiceTest {

    @Mock
    CaseDataService caseDataService;

    @InjectMocks
    CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(APPLICANT_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID("TestID").organisationName("TestName").build())
            .build());
        caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(caseData).build();
    }

    @Test
    public void givenContestedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
    }

    @Test
    public void givenConsentedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        caseDetails.setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
    }

    @Test
    public void givenOrganisationIsEmpty_whenCheckSolIsDigital_thenReturnFalse() {
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .organisation(null)
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .build());

        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertFalse(isSolicitorDigital);
    }
}
