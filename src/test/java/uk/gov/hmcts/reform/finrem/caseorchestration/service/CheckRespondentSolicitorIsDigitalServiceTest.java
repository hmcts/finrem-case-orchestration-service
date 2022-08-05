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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class CheckRespondentSolicitorIsDigitalServiceTest {

    @Mock
    CaseDataService caseDataService;

    @InjectMocks
    CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    private CaseDetails caseDetails;
    private FinremCaseDetails finremCaseDetails;

    @Before
    public void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID("TestIdResp").organisationName("TestName").build())
            .build());
        caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(caseData).build();

        FinremCaseData finremCaseData = new FinremCaseData();
        finremCaseData.setCcdCaseType(CaseType.CONTESTED);
        finremCaseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
        finremCaseData.setRespondentOrganisationPolicy(uk.gov.hmcts.reform.finrem.ccd.domain.OrganisationPolicy.builder()
            .organisation(uk.gov.hmcts.reform.finrem.ccd.domain.Organisation.builder().organisationID("TestID")
                .organisationName("TestName").build())
            .build());
        finremCaseDetails = FinremCaseDetails.builder().caseType(CaseType.CONTESTED).caseData(finremCaseData).build();
    }

    @Test
    public void givenContestedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        System.out.println(caseDataService);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
    }

    @Test
    public void givenConsentedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        caseDetails.setCaseTypeId(CASE_TYPE_ID_CONSENTED);
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
    }

    @Test
    public void givenOrganisationIsEmpty_whenCheckSolIsDigital_thenReturnFalse() {
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .organisation(null)
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .build());

        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertFalse(isSolicitorDigital);
    }

    @Test
    public void givenContestedCaseAndOrganisationPresent_whenCheckSolIsDigitalFinremCaseData_thenReturnTrue() {
        assertTrue(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseDetails));
    }

    @Test
    public void givenContestedCaseAndNoOrganisationPresent_whenCheckSolIsDigitalFinremCaseData_thenReturnFalse() {
        finremCaseDetails.getCaseData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        assertFalse(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseDetails));
    }
}
