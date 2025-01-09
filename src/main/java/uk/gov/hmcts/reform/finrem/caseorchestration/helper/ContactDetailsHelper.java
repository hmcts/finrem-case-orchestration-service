package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

public class ContactDetailsHelper {

    private static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    private static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    private static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    private static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";

    public ContactDetailsHelper() {
    }

    public static List<String> validateCaseData(FinremCaseData caseData) {

        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        List<String> errors1 = getStrings(caseData, errors, wrapper);
        if (errors1 != null) return errors1;

        if (wrapper.getApplicantAddress() != null
            && ObjectUtils.isEmpty(wrapper.getApplicantAddress().getPostCode())) {
            errors.add(APPLICANT_POSTCODE_ERROR);
            return errors;
        }

        if (caseData.isRespondentRepresentedByASolicitor()
            && wrapper.getRespondentSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentSolicitorAddress().getPostCode())) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }

        if (wrapper.getRespondentAddress() != null
            && ObjectUtils.isEmpty(wrapper.getRespondentAddress().getPostCode())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
            return errors;
        }


        return errors;
    }

    private static List<String> getStrings(FinremCaseData caseData, List<String> errors, ContactDetailsWrapper wrapper) {
        if (caseData.isApplicantRepresentedByASolicitor()
            && wrapper.getSolicitorAddress() != null
            && ObjectUtils.isEmpty(wrapper.getSolicitorAddress().getPostCode())) {
            errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            return errors;
        }
        return null;
    }


}
