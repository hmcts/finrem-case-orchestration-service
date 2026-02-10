package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateContactDetailsService {

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

    /**
     * Handles the representation change for a financial remedy case.
     * This method determines whether the Notice of Change (NoC) party is the applicant or respondent
     * and clears solicitor details if the party is unrepresented.
     *
     * @param caseData the financial remedy case data containing details about the case
     * @param caseType the type of the case (e.g., contested or consented)
     */
    public void handleRepresentationChange(FinremCaseData caseData, CaseType caseType) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();
        NoticeOfChangeParty nocParty = contactDetailsWrapper.getNocParty();

        if (NoticeOfChangeParty.APPLICANT.equals(nocParty)) {
            if (contactDetailsWrapper.getApplicantRepresented() == YesOrNo.NO) {
                clearApplicantSolicitorDetailsForUnrepresentedApplicant(caseData, caseType);
            }
            return;
        }

        if (NoticeOfChangeParty.RESPONDENT.equals(nocParty)) {
            boolean isContested = CaseType.CONTESTED.equals(caseType);

            YesOrNo respondentRepresented = isContested
                ? contactDetailsWrapper.getContestedRespondentRepresented()
                : contactDetailsWrapper.getConsentedRespondentRepresented();

            if (respondentRepresented == YesOrNo.NO) {
                clearRespondentSolicitorDetailsForUnrepresentedRespondent(caseData);
            }
        }
    }

    private void clearApplicantSolicitorDetailsForUnrepresentedApplicant(FinremCaseData caseData, CaseType caseType) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();

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

    private void clearRespondentSolicitorDetailsForUnrepresentedRespondent(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();

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
