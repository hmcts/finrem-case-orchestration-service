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

    /**
     * Determines if the postcode in the given address is invalid based on residency status.
     *
     * <p>
     * If the person resides in the UK (i.e., {@code resideOutsideUK} is {@code null} or {@code NO}),
     * the postcode is considered invalid if the address is empty or the postcode itself is empty.
     * If the person resides outside the UK, the emptiness of postcode is not considered invalid.
     * If the address is {@code null}, this method returns {@code false}.
     * </p>
     *
     * @param address the {@link Address} to validate
     * @param resideOutsideUK {@link YesOrNo} indicating if the person resides outside the UK; {@code null} means resides in the UK
     * @return {@code true} if the postcode is invalid for a UK resident, {@code false} otherwise
     */
    private static boolean postCodeIsInvalid(Address address, YesOrNo resideOutsideUK) {
        if (address == null) {
            return false;
        }
        boolean livesInUK = resideOutsideUK == null || YesOrNo.isNo(resideOutsideUK);

        if (livesInUK) {
            return address.isEmpty() || isEmpty(address.getPostCode());
        }
        return false;
    }

}
