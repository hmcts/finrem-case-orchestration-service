package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

public class ContactDetailsValidator {

    static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    private ContactDetailsValidator() {
    }

    public static List<String> validateCaseDataAddresses(FinremCaseData caseData) {

        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        if (caseData.isApplicantRepresentedByASolicitor()
            && wrapper.getSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getSolicitorAddress().getPostCode())) {
            errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        Address applicantAddress = wrapper.getApplicantAddress();

        if (applicantAddress != null
            && !applicantAddress.isEmpty()
            && ObjectUtils.isEmpty(applicantAddress.getPostCode())) {
            errors.add(APPLICANT_POSTCODE_ERROR);
            return errors;
        }

        if (caseData.isRespondentRepresentedByASolicitor()
            && wrapper.getRespondentSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentSolicitorAddress().getPostCode())) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        Address respondentAddress = wrapper.getRespondentAddress();

        if (respondentAddress != null
            && !respondentAddress.isEmpty()
            && ObjectUtils.isEmpty(respondentAddress.getPostCode())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
            return errors;
        }
        return errors;
    }
}
