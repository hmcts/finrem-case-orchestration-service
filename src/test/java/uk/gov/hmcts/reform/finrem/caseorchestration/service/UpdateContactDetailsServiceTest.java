package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class UpdateContactDetailsServiceTest extends BaseServiceTest {

    @Autowired
    UpdateContactDetailsService updateContactDetailsService;

    @Test
    public void shouldPersistOrgPolicies() {
        Map<String, Object> originalData = new HashMap<>();
        originalData.put(APPLICANT_ORGANISATION_POLICY, "ApplicantPolicyData");
        originalData.put(RESPONDENT_ORGANISATION_POLICY, "RespondentPolicyData");

        Map<String, Object> caseData = new HashMap<>();
        CaseDetails originalDetails = mock(CaseDetails.class);
        when(originalDetails.getData()).thenReturn(originalData);

        updateContactDetailsService.persistOrgPolicies(caseData, originalDetails);

        assertEquals("ApplicantPolicyData", caseData.get(APPLICANT_ORGANISATION_POLICY));
        assertEquals("RespondentPolicyData", caseData.get(RESPONDENT_ORGANISATION_POLICY));
    }

    @Test
    public void shouldReturnTrueIfRepresentationChangeIncluded() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(INCLUDES_REPRESENTATION_CHANGE, YES_VALUE);

        boolean result = updateContactDetailsService.isIncludesRepresentationChange(caseData);

        assertTrue(result);
    }

    @Test
    public void shouldRemoveApplicantSolicitorDetailsIfNocPartyIsApplicant() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, APPLICANT);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseTypeId()).thenReturn(CaseType.CONSENTED.getCcdType());
        when(caseDetails.getData()).thenReturn(caseData);

        updateContactDetailsService.handleApplicantRepresentationChange(caseDetails);


        assertFalse(caseData.containsKey(APPLICANT_REPRESENTED));
    }

    @Test
    public void shouldRemoveRespondentDetailsIfNocPartyIsRespondent() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, RESPONDENT);

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseTypeId()).thenReturn(CaseType.CONSENTED.getCcdType());
        when(caseDetails.getData()).thenReturn(caseData);

        updateContactDetailsService.handleRespondentRepresentationChange(caseDetails);

        assertFalse(caseData.containsKey(RESPONDENT_ADDRESS));
    }

    @Test
    public void shouldRemoveApplicantSolicitorDetailsForContestedCaseWhenNotRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, NO_VALUE);
        caseData.put(CONTESTED_SOLICITOR_NAME, "Solicitor Name");
        caseData.put(CONTESTED_SOLICITOR_FIRM, "Solicitor Firm");
        caseData.put(CONTESTED_SOLICITOR_ADDRESS, "Solicitor Address");
        caseData.put(CONTESTED_SOLICITOR_PHONE, "Solicitor Phone");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, "Solicitor Email");
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, "Yes");
        caseData.put(SOLICITOR_REFERENCE, "Reference");
        caseData.put(APPLICANT_ORGANISATION_POLICY, "Applicant Policy");

        String caseTypeId = CaseType.CONTESTED.getCcdType();

        UpdateContactDetailsService.removeApplicantSolicitorDetails(caseData, caseTypeId);

        assertFalse(caseData.containsKey(CONTESTED_SOLICITOR_NAME));
        assertFalse(caseData.containsKey(CONTESTED_SOLICITOR_FIRM));
        assertFalse(caseData.containsKey(CONTESTED_SOLICITOR_ADDRESS));
        assertFalse(caseData.containsKey(CONTESTED_SOLICITOR_PHONE));
        assertFalse(caseData.containsKey(CONTESTED_SOLICITOR_EMAIL));
        assertFalse(caseData.containsKey(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED));
        assertFalse(caseData.containsKey(SOLICITOR_REFERENCE));
        assertFalse(caseData.containsKey(APPLICANT_ORGANISATION_POLICY));
    }

    @Test
    public void shouldRemoveApplicantSolicitorDetailsForConsentedCaseWhenNotRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(APPLICANT_REPRESENTED, NO_VALUE);
        caseData.put(CONSENTED_SOLICITOR_NAME, "Solicitor Name");
        caseData.put(CONSENTED_SOLICITOR_FIRM, "Solicitor Firm");
        caseData.put(CONSENTED_SOLICITOR_ADDRESS, "Solicitor Address");
        caseData.put(SOLICITOR_PHONE, "Solicitor Phone");
        caseData.put(SOLICITOR_EMAIL, "Solicitor Email");
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, "Yes");
        caseData.put(SOLICITOR_REFERENCE, "Reference");
        caseData.put(CONSENTED_SOLICITOR_DX_NUMBER, "DX Number");
        caseData.put(APPLICANT_ORGANISATION_POLICY, "Applicant Policy");

        String caseTypeId = CaseType.CONSENTED.getCcdType();
        UpdateContactDetailsService.removeApplicantSolicitorDetails(caseData, caseTypeId);

        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_NAME));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_FIRM));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_ADDRESS));
        assertFalse(caseData.containsKey(SOLICITOR_PHONE));
        assertFalse(caseData.containsKey(SOLICITOR_EMAIL));
        assertFalse(caseData.containsKey(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED));
        assertFalse(caseData.containsKey(SOLICITOR_REFERENCE));
        assertFalse(caseData.containsKey(CONSENTED_SOLICITOR_DX_NUMBER));
        assertFalse(caseData.containsKey(APPLICANT_ORGANISATION_POLICY));
    }

    @Test
    public void shouldRemoveRespondentDetailsForContestedCaseWhenRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_ADDRESS, "Respondent Address");
        caseData.put(RESPONDENT_PHONE, "Respondent Phone");
        caseData.put(RESPONDENT_EMAIL, "Respondent Email");

        String caseTypeId = CaseType.CONTESTED.getCcdType();

        UpdateContactDetailsService.removeRespondentDetails(caseData, caseTypeId);

        assertFalse(caseData.containsKey(RESPONDENT_ADDRESS));
        assertFalse(caseData.containsKey(RESPONDENT_PHONE));
        assertFalse(caseData.containsKey(RESPONDENT_EMAIL));
        assertEquals(NO_VALUE, caseData.get(RESPONDENT_RESIDE_OUTSIDE_UK)); // This should be added
    }
}
