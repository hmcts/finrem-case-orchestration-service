package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@ExtendWith(MockitoExtension.class)
class CheckApplicantSolicitorIsDigitalServiceTest {

    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    private CaseDetails caseDetails;

    private static final OrganisationPolicy ORGANISATION_POLICY = OrganisationPolicy.builder()
        .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
        .organisation(Organisation.builder().organisationID(TEST_ORG_ID).organisationName("TestName").build())
        .build();

    @BeforeEach
    void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        caseData.put(APPLICANT_ORGANISATION_POLICY, ORGANISATION_POLICY);
        caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).id(Long.valueOf(CASE_ID)).data(caseData).build();
    }

    @Test
    void givenContestedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);
        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(caseDetails.getData());

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .applicantOrganisationPolicy(ORGANISATION_POLICY)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();

        when(caseDataService.isApplicantRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(finremCaseData);
    }

    @Test
    void givenConsentedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        caseDetails.setCaseTypeId(CaseType.CONSENTED.getCcdType());
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);

        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(caseDetails.getData());

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .applicantOrganisationPolicy(ORGANISATION_POLICY)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();

        when(caseDataService.isApplicantRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(finremCaseData);
    }

    @Test
    void givenOrganisationIsEmpty_whenCheckSolIsDigital_thenReturnFalse() {
        OrganisationPolicy emptyOrganisationPolicy = OrganisationPolicy.builder()
            .organisation(null)
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .build();

        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, emptyOrganisationPolicy);
        when(caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);
        boolean isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertFalse(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(caseDetails.getData());

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .applicantOrganisationPolicy(emptyOrganisationPolicy)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .build())
            .build();
        when(caseDataService.isApplicantRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertFalse(isSolicitorDigital);
        verify(caseDataService).isApplicantRepresentedByASolicitor(finremCaseData);
    }
}
