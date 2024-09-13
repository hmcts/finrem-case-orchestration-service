package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class UpdateContactDetailsServiceTest {

    private final UpdateContactDetailsService service = new UpdateContactDetailsService();

    @MockBean
    UpdateContactDetailsService updateContactDetailsService;

    @Test
    public void shouldPersistOrgPolicies() {
        Map<String, Object> originalData = new HashMap<>();
        originalData.put(APPLICANT_ORGANISATION_POLICY, "ApplicantPolicyData");
        originalData.put(RESPONDENT_ORGANISATION_POLICY, "RespondentPolicyData");

        Map<String, Object> caseData = new HashMap<>();
        CaseDetails originalDetails = mock(CaseDetails.class);
        when(originalDetails.getData()).thenReturn(originalData);

        service.persistOrgPolicies(caseData, originalDetails);

        assertEquals("ApplicantPolicyData", caseData.get(APPLICANT_ORGANISATION_POLICY));
        assertEquals("RespondentPolicyData", caseData.get(RESPONDENT_ORGANISATION_POLICY));
    }

    @Test
    public void shouldReturnTrueIfRepresentationChangeIncluded() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(INCLUDES_REPRESENTATION_CHANGE, YES_VALUE);

        boolean result = service.isIncludesRepresentationChange(caseData);

        assertTrue(result);
    }

    private static Map<String, Object> getContestedCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, APPLICANT);
        caseData.put(APPLICANT_REPRESENTED, NO_VALUE);
        caseData.put(CONTESTED_SOLICITOR_NAME, "Solicitor Name");
        caseData.put(CONTESTED_SOLICITOR_FIRM, "Solicitor Firm");
        caseData.put(CONTESTED_SOLICITOR_ADDRESS, "Solicitor Address");
        caseData.put(CONTESTED_SOLICITOR_PHONE, "Solicitor Phone");
        caseData.put(CONTESTED_SOLICITOR_EMAIL, "Solicitor Email");
        caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED, "Yes");
        caseData.put(SOLICITOR_REFERENCE, "Reference");
        caseData.put(APPLICANT_ORGANISATION_POLICY, "Applicant Policy");
        return caseData;
    }

    private static Map<String, Object> getConsentedCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, APPLICANT);
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
        return caseData;
    }

    private static String[] getContestedPropertiesToRemove() {
        return new String[]{
            CONTESTED_SOLICITOR_NAME,
            CONTESTED_SOLICITOR_FIRM,
            CONTESTED_SOLICITOR_ADDRESS,
            CONTESTED_SOLICITOR_PHONE,
            CONTESTED_SOLICITOR_EMAIL,
            APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED,
            SOLICITOR_REFERENCE,
            APPLICANT_ORGANISATION_POLICY
        };
    }

    private static String[] getConsentedPropertiesToRemove() {
        return new String[]{
            CONSENTED_SOLICITOR_NAME,
            CONSENTED_SOLICITOR_FIRM,
            CONSENTED_SOLICITOR_ADDRESS,
            SOLICITOR_PHONE,
            SOLICITOR_EMAIL,
            APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED,
            SOLICITOR_REFERENCE,
            CONSENTED_SOLICITOR_DX_NUMBER,
            APPLICANT_ORGANISATION_POLICY
        };
    }

    private static Stream<Arguments> applicantParameters() {
        return Stream.of(
            Arguments.of(CaseType.CONTESTED.getCcdType(), getContestedCaseData(), getContestedPropertiesToRemove()),
            Arguments.of(CaseType.CONSENTED.getCcdType(), getConsentedCaseData(), getConsentedPropertiesToRemove())
        );
    }

    @ParameterizedTest
    @MethodSource("applicantParameters")
    void shouldRemoveApplicantSolicitorDetails(String caseTypeId, Map<String, Object> caseData, String[] propertiesToRemove) {

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseDetails.getCaseTypeId()).thenReturn(caseTypeId);

        service.handleApplicantRepresentationChange(caseDetails);

        for (String property : propertiesToRemove) {
            assertFalse(caseData.containsKey(property), "property should be removed: " + property);
        }
    }

    private static Map<String, Object> getRespondentDataWhenRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, RESPONDENT);
        caseData.put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_ADDRESS, "Respondent Address");
        caseData.put(RESPONDENT_PHONE, "Respondent Phone");
        caseData.put(RESPONDENT_EMAIL, "Respondent Email");
        return caseData;
    }

    private static Map<String, Object> getRespondentSolicitorDataWhenNotRepresented() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(NOC_PARTY, RESPONDENT);
        caseData.put(CONTESTED_RESPONDENT_REPRESENTED, NO_VALUE);
        caseData.put(RESP_SOLICITOR_NAME, "Solicitor Name");
        caseData.put(RESP_SOLICITOR_FIRM, "Solicitor Firm");
        caseData.put(RESP_SOLICITOR_ADDRESS, "Solicitor Address");
        caseData.put(RESP_SOLICITOR_PHONE, "Solicitor Phone");
        caseData.put(RESP_SOLICITOR_EMAIL, "Solicitor Email");
        caseData.put(RESP_SOLICITOR_DX_NUMBER, "DX Number");
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, "Yes");
        caseData.put(RESPONDENT_ORGANISATION_POLICY, "Respondent Policy");
        return caseData;
    }

    private static String[] getRespondentPropertiesToRemoveWhenRepresented() {
        return new String[]{
            RESPONDENT_ADDRESS,
            RESPONDENT_PHONE,
            RESPONDENT_EMAIL
        };
    }

    private static String[] getRespondentPropertiesToRemoveWhenNotRepresented() {
        return new String[]{
            RESP_SOLICITOR_NAME,
            RESP_SOLICITOR_FIRM,
            RESP_SOLICITOR_ADDRESS,
            RESP_SOLICITOR_PHONE,
            RESP_SOLICITOR_EMAIL,
            RESP_SOLICITOR_DX_NUMBER,
            RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT,
            RESPONDENT_ORGANISATION_POLICY
        };
    }

    private static Stream<Arguments> respondentParameters() {
        return Stream.of(
            Arguments.of(CaseType.CONSENTED.getCcdType(), getRespondentDataWhenRepresented(),
                getRespondentPropertiesToRemoveWhenRepresented()),
            Arguments.of(CaseType.CONTESTED.getCcdType(), getRespondentSolicitorDataWhenNotRepresented(),
                getRespondentPropertiesToRemoveWhenNotRepresented())
        );
    }

    @ParameterizedTest
    @MethodSource("respondentParameters")
    void shouldHandleRespondentDetails(String caseTypeId, Map<String, Object> caseData, String[] propertiesToRemove) {

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseDetails.getCaseTypeId()).thenReturn(caseTypeId);

        service.handleRespondentRepresentationChange(caseDetails);

        for (String property : propertiesToRemove) {
            assertFalse(caseData.containsKey(property), "Property should be removed: " + property);
        }

    }
}

