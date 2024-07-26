package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.NullChecker;

import java.util.ArrayList;
import java.util.List;

@Component
class ApplicantSolicitorDetailsValidator implements MandatoryDataValidator {
    @Override
    public List<String> validate(FinremCaseData caseData) {
        ContactDetailsWrapper contactDetailsWrapper = caseData.getContactDetailsWrapper();

        List<String> ret = new ArrayList<>();
        if (!NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorAddress())) {
            ret.add("Applicant solicitor's address is required.");
        }
        if (!NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorEmail())) {
            ret.add("Applicant solicitor's email is required.");
        }
        if (!NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorPhone())) {
            ret.add("Applicant solicitor's phone is required.");
        }
        if (!NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorFirm())) {
            ret.add("Applicant solicitor's name of your firm is required.");
        }
        if (!NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorName())) {
            ret.add("Applicant solicitor's name is required.");
        }
        return ret;
    }
}
