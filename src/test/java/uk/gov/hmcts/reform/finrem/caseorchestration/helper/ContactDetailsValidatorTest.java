package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_SOLICITOR_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_SOLICITOR_POSTCODE_ERROR;


class ContactDetailsValidatorTest {

    @ParameterizedTest
    @MethodSource("provideInvalidCaseData")
    void shouldReturnErrorWhenPostcodeIsMissing(FinremCaseData caseData, String expectedError) {
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertEquals(1, errors.size(), "Expected only one error.");
        assertEquals(expectedError, errors.get(0), "Error message mismatch.");
    }

    private static Stream<Object[]> provideInvalidCaseData() {
        return Stream.of(
            new Object[] {
                createCaseData(null, "SW1A 1AA", "E1 6AN", "EC1A 1BB", YesOrNo.YES, null, null), APPLICANT_SOLICITOR_POSTCODE_ERROR  },
            new Object[]{ createCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null, null), APPLICANT_POSTCODE_ERROR },
            new Object[]{ createCaseData("SW1A 1AA", "E1 6AN", null, "EC1A 1BB", null, YesOrNo.YES, null), RESPONDENT_SOLICITOR_POSTCODE_ERROR},
            new Object[]{ createCaseData("SW1A 1AA", "E1 6AN", null, "EC1A 1BB", null, null, YesOrNo.YES), RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[]{ createCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", null, null, null, null), RESPONDENT_POSTCODE_ERROR }
        );
    }

    @Test
    void shouldReturnNoErrorsForValidCaseData() {
        FinremCaseData caseData = createCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null, null);
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertTrue(errors.isEmpty(), "Errors should be empty for valid case data.");
    }

    @Test
    void shouldNotReturnErrorWhenApplicantAddressIsEmptyOrNull() {
        FinremCaseData caseData = createCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setApplicantAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertTrue(errors.isEmpty(), "No errors should be returned when applicant address is empty or null.");
    }

    @Test
    void shouldNotReturnErrorWhenRespondentAddressIsEmptyOrNull() {
        FinremCaseData caseData = createCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setRespondentAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertTrue(errors.isEmpty(), "No errors should be returned when respondent address is empty or null.");
    }

    private static FinremCaseData createCaseData(String applicantSolicitorPostcode, String applicantPostcode,
                                                 String respondentSolicitorPostcode, String respondentPostcode,
                                                 YesOrNo applicantRepresented, YesOrNo contestedRespondentRepresented,
                                                 YesOrNo consentedRespondentRepresented) {
        ContactDetailsWrapper wrapper = new ContactDetailsWrapper();

        wrapper.setSolicitorAddress(new Address("AddressLine1", "AddressLine2", "AddressLine3", "County", "Country", "Town",
            applicantSolicitorPostcode));
        wrapper.setApplicantAddress(new Address("AddressLine1", "AddressLine2", "AddressLine3", "County", "Country", "Town",
            applicantPostcode));
        wrapper.setRespondentSolicitorAddress(new Address("AddressLine1", "AddressLine2", "AddressLine3", "County", "Country", "Town",
            respondentSolicitorPostcode));
        wrapper.setRespondentAddress(new Address("AddressLine1", "AddressLine2", "AddressLine3", "County", "Country", "Town",
            respondentPostcode));

        wrapper.setApplicantRepresented(applicantRepresented);
        wrapper.setContestedRespondentRepresented(contestedRespondentRepresented);
        wrapper.setConsentedRespondentRepresented(consentedRespondentRepresented);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setContactDetailsWrapper(wrapper);
        return caseData;
    }
}
