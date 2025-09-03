package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.EmailUtils.isValidEmailAddress;

public class ContactDetailsValidator {

    static final String APPLICANT_POSTCODE_ERROR = "Postcode field is required for applicant address.";
    static final String RESPONDENT_POSTCODE_ERROR = "Postcode field is required for respondent address.";
    static final String APPLICANT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for applicant solicitor address.";
    static final String RESPONDENT_SOLICITOR_POSTCODE_ERROR = "Postcode field is required for respondent solicitor address.";
    static final String INVALID_EMAIL_ADDRESS_ERROR_MESSAGE = "%s is not a valid Email address.";

    private ContactDetailsValidator() {
    }

    /**
     * Validates the postcode fields within the contact details of the given case data.
     *
     * <p>
     * This method checks for missing or empty postcode fields depending on whether the applicant,
     * respondent, or their solicitors are expected to reside in the UK.
     * </p>
     *
     * <ul>
     *   <li>Applicant solicitor postcode (if applicable)</li>
     *   <li>Applicant postcode (if applicable)</li>
     *   <li>Respondent solicitor postcode (if applicable)</li>
     *   <li>Respondent postcode (if applicable)</li>
     * </ul>
     *
     * @param caseData the {@link FinremCaseData} containing contact details to validate
     * @return a list of validation error messages for any missing or invalid postcode fields
     */
    public static List<String> validateCaseDataAddresses(FinremCaseData caseData) {
        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, errors);
        checkForEmptyApplicantPostcode(wrapper, errors);
        checkForEmptyRespondentSolicitorPostcode(caseData, wrapper, errors);
        checkForEmptyRespondentPostcode(wrapper, errors);

        return errors;
    }

    /**
     * Validates the applicant solicitor's postcode and adds an error if it is missing or invalid.
     *
     * <p>
     * For contested cases, the postcode is taken from {@code wrapper.getApplicantSolicitorAddress()}.
     * For consented cases, the postcode is taken from {@code wrapper.getSolicitorAddress()}.
     * </p>
     *
     * <p>
     * If the applicant is represented by a solicitor and the postcode is missing or invalid,
     * an error message is added to the provided {@code errors} list.
     * </p>
     *
     * @param caseData the case data containing case type and solicitor representation info
     * @param wrapper the wrapper containing solicitor address details
     * @param errors the list to which error messages will be added if validation fails
     */
    public static void checkForEmptyApplicantSolicitorPostcode(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        if (isContested(caseData)) {
            if (caseData.isApplicantRepresentedByASolicitor()
                && postCodeIsInvalid(wrapper.getApplicantSolicitorAddress())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        } else if (isConsented(caseData)) {
            if (caseData.isApplicantRepresentedByASolicitor()
                && postCodeIsInvalid(wrapper.getSolicitorAddress())) {
                errors.add(APPLICANT_SOLICITOR_POSTCODE_ERROR);
            }
        }
    }

    /**
     * Validates the applicant's postcode and adds an error if it is missing or invalid.
     *
     * <p>
     * If the applicant resides in the UK (or residency is unspecified) and the postcode is missing or empty,
     * an error message is added to the provided {@code errors} list.
     * </p>
     *
     * <p>
     * If the applicant resides outside the UK, postcode validation is skipped.
     * </p>
     *
     * @param wrapper the wrapper containing applicant address and residency details
     * @param errors the list to which error messages will be added if validation fails
     */
    public static void checkForEmptyApplicantPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
        Address applicantAddress = wrapper.getApplicantAddress();
        if (postCodeIsInvalid(applicantAddress, wrapper.getApplicantResideOutsideUK())) {
            errors.add(APPLICANT_POSTCODE_ERROR);
        }
    }

    /**
     * Validates the respondent solicitor's postcode and adds an error if it is missing or invalid.
     *
     * <p>
     * If the respondent is represented by a solicitor and the postcode in
     * {@code wrapper.getRespondentSolicitorAddress()} is missing or empty, an error message is added
     * to the provided {@code errors} list.
     * </p>
     *
     * @param caseData the case data indicating solicitor representation status
     * @param wrapper the wrapper containing respondent solicitor address details
     * @param errors the list to which error messages will be added if validation fails
     */
    public static void checkForEmptyRespondentSolicitorPostcode(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        if (caseData.isRespondentRepresentedByASolicitor()
            && postCodeIsInvalid(wrapper.getRespondentSolicitorAddress())) {
            errors.add(RESPONDENT_SOLICITOR_POSTCODE_ERROR);
        }
    }

    /**
     * Validates the respondent's postcode and adds an error if it is missing or invalid.
     *
     * <p>
     * If the respondent resides in the UK (or if residency is unspecified) and the postcode in
     * {@code wrapper.getRespondentAddress()} is missing or empty, an error message is added
     * to the provided {@code errors} list.
     * </p>
     *
     * <p>
     * If the respondent resides outside the UK, postcode validation is skipped.
     * </p>
     *
     * @param wrapper the wrapper containing respondent address and residency details
     * @param errors the list to which error messages will be added if validation fails
     */
    public static void checkForEmptyRespondentPostcode(ContactDetailsWrapper wrapper, List<String> errors) {
        Address respondentAddress = wrapper.getRespondentAddress();
        if (postCodeIsInvalid(respondentAddress, wrapper.getRespondentResideOutsideUK())) {
            errors.add(RESPONDENT_POSTCODE_ERROR);
        }
    }

    /**
     * Validates the email addresses present in the given {@link FinremCaseData}.
     *
     * <p>
     * This method checks the following email fields:
     * <ul>
     *     <li>Applicant solicitor's email address</li>
     *     <li>Applicant's email address</li>
     *     <li>Respondent solicitor's email address</li>
     *     <li>Respondent's email address</li>
     * </ul>
     * Any validation errors found are added to a list and returned.
     *
     * @param caseData the case data containing contact details to be validated
     * @return a list of error messages for invalid email addresses
     */
    public static List<String> validateCaseDataEmailAddresses(FinremCaseData caseData) {
        List<String> errors = new ArrayList<>();
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        checkForApplicantSolicitorEmailAddress(caseData, wrapper, errors);
        checkForApplicantEmail(wrapper, errors);
        checkForRespondentSolicitorEmail(caseData, wrapper, errors);
        checkForRespondentEmail(caseData, wrapper, errors);

        return errors;
    }

    /**
     * Validates the given solicitor address assuming the solicitor is based in the UK.
     *
     * <p>
     * This method is typically used for validating applicant or respondent solicitor addresses,
     * where it is expected that the solicitor resides within the UK.
     * Therefore, the {@code resideOutsideUK} flag is assumed to be {@link YesOrNo#NO}.
     * </p>
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
     * </p>
     *
     * <p>
     * If the person lives outside the UK, address validation is not enforced (always returns false).
     * If the {@code resideOutsideUK} field is not set and the address is empty or null,
     * the address is not considered invalid.
     * </p>
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

    private static void checkForApplicantSolicitorEmailAddress(FinremCaseData caseData, ContactDetailsWrapper wrapper,
                                                               List<String> errors) {
        if (isContested(caseData)) {
            if (caseData.isApplicantRepresentedByASolicitor()
                && !isValidEmailAddress(wrapper.getApplicantSolicitorEmail())) {
                errors.add(format(INVALID_EMAIL_ADDRESS_ERROR_MESSAGE, wrapper.getApplicantSolicitorEmail()));
            }
        } else if (isConsented(caseData)) {
            if (caseData.isApplicantRepresentedByASolicitor()
                && !isValidEmailAddress(wrapper.getSolicitorEmail())) {
                errors.add(format(INVALID_EMAIL_ADDRESS_ERROR_MESSAGE, wrapper.getSolicitorEmail()));
            }
        }
    }

    private static void checkForApplicantEmail(ContactDetailsWrapper wrapper, List<String> errors) {
        String applicantEmail = wrapper.getApplicantEmail();
        if (!isValidEmailAddress(applicantEmail, true)) {
            errors.add(format(INVALID_EMAIL_ADDRESS_ERROR_MESSAGE, applicantEmail));
        }
    }

    private static void checkForRespondentSolicitorEmail(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        if (caseData.isRespondentRepresentedByASolicitor()
            && !isValidEmailAddress(wrapper.getRespondentSolicitorEmail(), true)) {
            errors.add(format(INVALID_EMAIL_ADDRESS_ERROR_MESSAGE, wrapper.getRespondentSolicitorEmail()));
        }
    }

    private static void checkForRespondentEmail(FinremCaseData caseData, ContactDetailsWrapper wrapper, List<String> errors) {
        String respondentEmail = wrapper.getRespondentEmail();
        if (!caseData.isRespondentRepresentedByASolicitor()) {
            if (!isValidEmailAddress(respondentEmail, true)) {
                errors.add(format(INVALID_EMAIL_ADDRESS_ERROR_MESSAGE, respondentEmail));
            }
        }
    }

    private static boolean isContested(FinremCaseData caseData) {
        return caseData.getCcdCaseType() == CaseType.CONTESTED;
    }

    private static boolean isConsented(FinremCaseData caseData) {
        return caseData.getCcdCaseType() == CaseType.CONSENTED;
    }
}
