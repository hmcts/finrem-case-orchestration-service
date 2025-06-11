package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
                && postCodeIsInvalid(wrapper.getApplicantSolicitorAddress())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        } else {
            if (caseData.isApplicantRepresentedByASolicitor()
                && postCodeIsInvalid(wrapper.getSolicitorAddress())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        }
    }

    public static void checkForEmptyApplicantPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
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

    public static void checkForEmptyRespondentPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
        Address respondentAddress = wrapper.getRespondentAddress();
        if (postCodeIsInvalid(respondentAddress, wrapper.getRespondentResideOutsideUK())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
        }
    }

    /**
     * Validates the given solicitor address assuming the solicitor is based in the UK.
     *
     * <p>
     * This method is typically used for validating applicant or respondent solicitor addresses,
     * where it is expected that the solicitor resides within the UK.
     * Therefore, the {@code resideOutsideUK} flag is assumed to be {@link YesOrNo#NO}.
     *
     * @param address the solicitor's address to validate
     * @return {@code true} if the address is empty or missing a postcode; {@code false} otherwise
     */
    private static boolean postCodeIsInvalid(Address address) {
        return postCodeIsInvalid(address, YesOrNo.NO);
    }

    /**
     * Determines whether the given address is invalid based on whether the person lives in the UK.
     *
     * <p>
     * An address is considered invalid if:
     * <ul>
     *   <li>The person lives in the UK (either explicitly or because the field is not set), and</li>
     *   <li>The address is empty or missing a postcode.</li>
     * </ul>
     *
     * <p>
     * If the person lives outside the UK, address validation is not enforced (always returns false).
     * If the {@code resideOutsideUK} field is not set and the address is empty or null,
     * the address is not considered invalid.
     *
     * @param address           the address to validate
     * @param resideOutsideUK   {@code YES} if the person lives outside the UK, {@code NO} if they live in the UK,
     *                          or {@code null} if not answered
     * @return {@code true} if the postcode is invalid for someone living in the UK; {@code false} otherwise
     */
    private static boolean postCodeIsInvalid(Address address, YesOrNo resideOutsideUK) {
        // If address is null, or field is unanswered and address is empty, treat as valid
        if (address == null || (resideOutsideUK == null && address.isEmpty())) {
            return false;
        }

        // Treat as living in the UK if field is not answered or explicitly set to NO
        boolean livesInUK = resideOutsideUK == null || YesOrNo.isNo(resideOutsideUK);

        // For UK residents, postcode must be present and address must not be empty
        boolean addressMissingRequiredPostcode = address.isEmpty() || isBlank(address.getPostCode());

        return livesInUK && addressMissingRequiredPostcode;
    }
}
