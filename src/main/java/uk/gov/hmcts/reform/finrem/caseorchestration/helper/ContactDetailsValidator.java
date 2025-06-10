package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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

        checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, errors);
        checkForEmptyApplicantPostcode(wrapper, errors);
        checkForEmptyRespondentSolicitorPostcode(caseData, wrapper, errors);
        checkForEmptyRespondentPostcode(wrapper, errors);

        return errors;
    }

    private static void checkForEmptyApplicantSolicitorPostcode(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        if (caseData.getCcdCaseType() == CaseType.CONTESTED) {
            if (caseData.isApplicantRepresentedByASolicitor()
                && wrapper.getApplicantSolicitorAddress() != null
                && ObjectUtils.isEmpty(wrapper.getApplicantSolicitorAddress().getPostCode())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        } else {
            if (caseData.isApplicantRepresentedByASolicitor()
                && wrapper.getSolicitorAddress() != null
                && ObjectUtils.isEmpty(wrapper.getSolicitorAddress().getPostCode())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        }
    }

    private static void checkForEmptyApplicantPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
        Address applicantAddress = wrapper.getApplicantAddress();
        if (postCodeIsInvalid(applicantAddress, wrapper.getApplicantResideOutsideUK())) {
            errors.add(APPLICANT_POSTCODE_ERROR);
        }

    }

    private static void checkForEmptyRespondentSolicitorPostcode(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        if (caseData.isRespondentRepresentedByASolicitor()
            && postCodeIsInvalid(wrapper.getRespondentSolicitorAddress())) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
        }
    }

    private static void checkForEmptyRespondentPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
        Address respondentAddress = wrapper.getRespondentAddress();
        if (postCodeIsInvalid(respondentAddress, wrapper.getRespondentResideOutsideUK())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
        }
    }

    private static boolean postCodeIsInvalid(Address address) {
        return postCodeIsInvalid(address, null);
    }

    private static boolean postCodeIsInvalid(Address address, YesOrNo resideOutsideUK) {
        return address != null && !address.isEmpty() && YesOrNo.isNoOrNull(resideOutsideUK) && isEmpty(address.getPostCode());
    }
}
