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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@ExtendWith(MockitoExtension.class)
class CheckRespondentSolicitorIsDigitalServiceTest {

    private static final OrganisationPolicy ORGANISATION_POLICY = OrganisationPolicy.builder()
        .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
        .organisation(Organisation.builder().organisationID(TEST_ORG_ID).organisationName("TestName").build())
        .build();

    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_ORGANISATION_POLICY, ORGANISATION_POLICY);
        caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).id(Long.valueOf(CASE_ID)).data(caseData).build();
    }

    @Test
    void givenContestedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);
        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(caseDetails.getData());

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .respondentOrganisationPolicy(ORGANISATION_POLICY)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .contestedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        reset(caseDataService);

        when(caseDataService.isRespondentRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(finremCaseData);
    }

    @Test
    void givenConsentedCaseAndOrganisationIsPresent_whenCheckSolIsDigital_thenReturnTrue() {
        caseDetails.setCaseTypeId(CaseType.CONSENTED.getCcdType());
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);
        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(caseDetails.getData());

        reset(caseDataService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .respondentOrganisationPolicy(ORGANISATION_POLICY)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        when(caseDataService.isRespondentRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertTrue(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(finremCaseData);
    }

    @Test
    void givenOrganisationIsEmpty_whenCheckSolIsDigital_thenReturnFalse() {
        OrganisationPolicy emptyOrganisationPolicy = OrganisationPolicy.builder()
            .organisation(null)
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .build();

        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, emptyOrganisationPolicy);

        when(caseDataService.isRespondentRepresentedByASolicitor(caseDetails.getData())).thenReturn(true);
        boolean isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
        assertFalse(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(caseDetails.getData());

        reset(caseDataService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .respondentOrganisationPolicy(emptyOrganisationPolicy)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .contestedRespondentRepresented(YesOrNo.YES)
                .build())
            .build();

        when(caseDataService.isRespondentRepresentedByASolicitor(finremCaseData)).thenReturn(true);
        isSolicitorDigital = checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData);
        assertFalse(isSolicitorDigital);
        verify(caseDataService).isRespondentRepresentedByASolicitor(finremCaseData);
    }
}
