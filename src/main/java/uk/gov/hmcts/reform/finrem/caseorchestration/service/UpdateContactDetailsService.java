package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.Map;
import java.util.Optional;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateContactDetailsService {

    @Deprecated
    public void persistOrgPolicies(Map<String, Object> caseData, CaseDetails originalDetails) {
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        caseData.put(RESPONDENT_ORGANISATION_POLICY, originalDetails.getData().get(RESPONDENT_ORGANISATION_POLICY));
    }

    /**
     * Persists the organisation policies through the callback request.
     *
     * @param  finremCaseData  the case data to persist org policies against
     * @param  originalFinremCaseData the case data prior to submit request to persist org policies from
     */
    public void persistOrgPolicies(FinremCaseData finremCaseData, FinremCaseData originalFinremCaseData) {
        finremCaseData.setApplicantOrganisationPolicy(originalFinremCaseData.getApplicantOrganisationPolicy());
        finremCaseData.setRespondentOrganisationPolicy(originalFinremCaseData.getRespondentOrganisationPolicy());
    }

    @Deprecated
    public boolean isIncludesRepresentationChange(Map<String, Object> caseData) {
        return YES_VALUE.equals(caseData.get(INCLUDES_REPRESENTATION_CHANGE));
    }

    @Deprecated
    public void handleApplicantRepresentationChange(CaseDetails caseDetails) {
        String caseTypeId = caseDetails.getCaseTypeId();
        Map<String, Object> caseData = caseDetails.getData();

        String nocParty = (String) caseData.get(NOC_PARTY);
        if (APPLICANT.equalsIgnoreCase(nocParty)) {
            removeApplicantSolicitorDetails(caseData, caseTypeId);
        }
    }

    @Deprecated
    public void handleRespondentRepresentationChange(CaseDetails caseDetails) {
        String caseTypeId = caseDetails.getCaseTypeId();
        Map<String, Object> caseData = caseDetails.getData();

        String nocParty = (String) caseData.get(NOC_PARTY);
        if (RESPONDENT.equalsIgnoreCase(nocParty)) {
            removeRespondentDetails(caseData, caseTypeId);
        }
    }

    /**
     * Orchestrates representation change for applicant and respondent.
     *
     * @param  caseData  FinremCaseData to apply representation change to
     * @param  caseType CaseType of the case to change representation.
     */
    public void handleRepresentationChange(FinremCaseData caseData, CaseType caseType) {
        String nocPart = caseData.getContactDetailsWrapper().getNocParty().getValue();
        if (APPLICANT.equalsIgnoreCase(nocPart)) {
            removeApplicantSolicitorDetails(caseData, caseType);
        }

        if (RESPONDENT.equalsIgnoreCase(nocPart)) {
            removeRespondentDetails(caseData, caseType);
        }
    }

    @Deprecated
    void removeApplicantSolicitorDetails(Map<String, Object> caseData, String caseTypeId) {
        boolean isContested = caseTypeId.equalsIgnoreCase(CaseType.CONTESTED.getCcdType());
        String applicantRepresented = nullToEmpty(caseData.get(APPLICANT_REPRESENTED));
        if (applicantRepresented.equals(NO_VALUE)) {
            caseData.remove(isContested ? CONTESTED_SOLICITOR_NAME : CONSENTED_SOLICITOR_NAME);
            caseData.remove(isContested ? CONTESTED_SOLICITOR_FIRM : CONSENTED_SOLICITOR_FIRM);
            caseData.remove(isContested ? CONTESTED_SOLICITOR_ADDRESS : CONSENTED_SOLICITOR_ADDRESS);
            caseData.remove(isContested ? CONTESTED_SOLICITOR_PHONE : SOLICITOR_PHONE);
            caseData.remove(isContested ? CONTESTED_SOLICITOR_EMAIL : SOLICITOR_EMAIL);
            caseData.remove(isContested ? APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED : APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED);

            caseData.remove(SOLICITOR_REFERENCE);
            caseData.remove(isContested ? null : CONSENTED_SOLICITOR_DX_NUMBER);
            caseData.remove(APPLICANT_ORGANISATION_POLICY);
        }
    }

    private void removeApplicantSolicitorDetails(FinremCaseData caseData, CaseType caseType) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        Optional<YesOrNo> isApplicantRepresented = Optional.ofNullable(contactDetailsWrapper.getApplicantRepresented());

        if (isApplicantRepresented.isEmpty()
            || isApplicantRepresented.get() == YesOrNo.NO) {
            if (CaseType.CONTESTED.equals(caseType)) {
                contactDetailsWrapper.setApplicantSolicitorName(null);
                contactDetailsWrapper.setApplicantSolicitorFirm(null);
                contactDetailsWrapper.setApplicantSolicitorAddress(null);
                contactDetailsWrapper.setApplicantSolicitorPhone(null);
                contactDetailsWrapper.setApplicantSolicitorEmail(null);
                contactDetailsWrapper.setApplicantSolicitorConsentForEmails(null);
            } else {
                contactDetailsWrapper.setSolicitorName(null);
                contactDetailsWrapper.setSolicitorFirm(null);
                contactDetailsWrapper.setSolicitorAddress(null);
                contactDetailsWrapper.setSolicitorPhone(null);
                contactDetailsWrapper.setSolicitorEmail(null);
                contactDetailsWrapper.setSolicitorAgreeToReceiveEmails(null);
            }
            contactDetailsWrapper.setSolicitorReference(null);
            caseData.setApplicantOrganisationPolicy(null);
        }
    }

    @Deprecated
    void removeRespondentDetails(Map<String, Object> caseData, String caseTypeId) {
        boolean isContested = caseTypeId.equalsIgnoreCase(CaseType.CONTESTED.getCcdType());

        String respondentRepresented = isContested
            ? nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED))
            : nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED));

        if (respondentRepresented.equals(YES_VALUE)) {
            caseData.remove(RESPONDENT_ADDRESS);
            caseData.remove(RESPONDENT_PHONE);
            caseData.remove(RESPONDENT_EMAIL);
            caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, NO_VALUE);
        } else {
            caseData.remove(RESP_SOLICITOR_NAME);
            caseData.remove(RESP_SOLICITOR_FIRM);
            caseData.remove(RESP_SOLICITOR_ADDRESS);
            caseData.remove(RESP_SOLICITOR_PHONE);
            caseData.remove(RESP_SOLICITOR_EMAIL);
            caseData.remove(RESP_SOLICITOR_DX_NUMBER);
            caseData.remove(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT);
            caseData.remove(RESPONDENT_ORGANISATION_POLICY);
        }
    }

    private void removeRespondentDetails(FinremCaseData caseData, CaseType caseType) {
        boolean isContested = CaseType.CONTESTED.equals(caseType);

        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        Optional<YesOrNo> isRespondentRepresented =
            Optional.ofNullable(isContested
                ? contactDetailsWrapper.getContestedRespondentRepresented()
                : contactDetailsWrapper.getConsentedRespondentRepresented());

        if (isRespondentRepresented.isPresent()
            && isRespondentRepresented.get() == YesOrNo.YES) {
            contactDetailsWrapper.setRespondentSolicitorAddress(null);
            contactDetailsWrapper.setRespondentSolicitorPhone(null);
            contactDetailsWrapper.setRespondentResideOutsideUK(YesOrNo.NO);
        } else {
            contactDetailsWrapper.setRespondentSolicitorName(null);
            contactDetailsWrapper.setRespondentSolicitorFirm(null);
            contactDetailsWrapper.setRespondentSolicitorAddress(null);
            contactDetailsWrapper.setRespondentSolicitorPhone(null);
            contactDetailsWrapper.setRespondentSolicitorEmail(null);
            contactDetailsWrapper.setRespondentSolicitorDxNumber(null);
            caseData.setRespSolNotificationsEmailConsent(null);
            caseData.setRespondentOrganisationPolicy(null);
        }
    }
}
