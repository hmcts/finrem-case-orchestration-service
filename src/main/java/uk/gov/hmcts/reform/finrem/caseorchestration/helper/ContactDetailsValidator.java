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
     * Checks if the postcode in the given address is invalid based on the residence status.
     *
     * <p>The logic is as follows:
     * <ul>
     *   <li>If {@code resideOutsideUK} is {@code null}, it is assumed the person resides in the UK (treated as {@code No}).</li>
     *   <li>If {@code address} is {@code null}, returns {@code false} since there's no postcode to validate.</li>
     *   <li>If the person resides in the UK ({@code resideOutsideUK} is {@code No}) and the address is empty, returns {@code true} indicating invalid postcode.</li>
     *   <li>If the address is not empty, the person resides in the UK or the residence status is unknown, and the postcode is empty, returns {@code true} indicating invalid postcode.</li>
     * </ul>
     *
     * @param address the {@link Address} object containing address details, may be {@code null}
     * @param resideOutsideUK the {@link YesOrNo} flag indicating if the person resides outside the UK; if {@code null}, assumed to be {@code No}
     * @return {@code true} if the postcode is considered invalid under the given conditions; {@code false} otherwise
     */
    private static boolean postCodeIsInvalid(Address address, YesOrNo resideOutsideUK) {
        resideOutsideUK = resideOutsideUK == null ? YesOrNo.NO : resideOutsideUK; // living in UK if resideOutsideUK is null
        if (address == null) {
            return false;
        }
        if (YesOrNo.isNo(resideOutsideUK) && address.isEmpty()) {
            return true;
        }
        return !address.isEmpty() && YesOrNo.isNoOrNull(resideOutsideUK) && isEmpty(address.getPostCode());
    }
}
