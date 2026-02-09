package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.Optional;

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
     * Orchestrates the representation change for the applicant or respondent,
     * based on the value selected in the NoC field
     * labelled: "Select Party to which the change in representation applies:".
     *
     * <p>
     * If the applicant is selected, this method clears the applicant solicitor
     * details according to the provided {@code caseType}. If the respondent is
     * selected, it clears the respondent solicitor details accordingly.
     *
     * @param caseData the {@link FinremCaseData} instance to update
     * @param caseType the {@link CaseType} used to determine which solicitor
     *                 contact details should be cleared
     */
    public void handleRepresentationChange(FinremCaseData caseData, CaseType caseType) {
        NoticeOfChangeParty nocPart = caseData.getContactDetailsWrapper().getNocParty();
        if (NoticeOfChangeParty.APPLICANT.equals(nocPart)) {
            removeApplicantSolicitorDetails(caseData, caseType);
        }

        if (NoticeOfChangeParty.RESPONDENT.equals(nocPart)) {
            removeRespondentDetails(caseData, caseType);
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
