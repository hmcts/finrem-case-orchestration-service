package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_SOLICITOR_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_SOLICITOR_POSTCODE_ERROR;

class ContactDetailsValidatorTest {

    private static class PostCodeModifier {

        String postCode;
        YesOrNo resideOutsideUK;

        PostCodeModifier(String postCode) {
            this.postCode = postCode;
            this.resideOutsideUK = null;
        }

        PostCodeModifier(String postCode, boolean resideOutsideUK) {
            this.postCode = postCode;
            this.resideOutsideUK = YesOrNo.forValue(resideOutsideUK);
        }
    }

    @ParameterizedTest
    @MethodSource("provideCaseDataWithInvalidPostcodes")
    void shouldValidatePostcodeConditionsAndReturnExpectedErrors(FinremCaseData caseData, String expectedError) {
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        if (expectedError != null) {
            assertThat(errors).containsOnly(expectedError);
        } else {
            assertThat(errors).isEmpty();
        }
    }

    private static Stream<Object[]> provideCaseDataWithInvalidPostcodes() {
        return Stream.of(
            new Object[] {
                createConsentedCaseData(null, new PostCodeModifier("SW1A 1AA"), "E1 6AN", new PostCodeModifier("EC1A 1BB"), YesOrNo.YES, null, null),
                APPLICANT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(null, false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(" ", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(null, true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(" ", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, YesOrNo.YES, null),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, null, YesOrNo.YES),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null), null, null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(""), null, null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" "), null, null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier("", true), null, null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" ", true), null, null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null, true), null, null, null),
                null }
        );
    }

    @Test
    void shouldReturnNoErrorsForValidCaseData() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null, null);
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenApplicantAddressIsEmpty() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setApplicantAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenRespondentAddressIsEmpty() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setRespondentAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenApplicantAddressIsNull() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setApplicantAddress(null); // Null address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenRespondentAddressIsNull() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null, null);
        caseData.getContactDetailsWrapper().setRespondentAddress(null); // Null address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    private static FinremCaseData createConsentedCaseData(String applicantSolicitorPostcode, String applicantPostcode,
                                                          String respondentSolicitorPostcode, String respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo contestedRespondentRepresented,
                                                          YesOrNo consentedRespondentRepresented) {
        return createConsentedCaseData(
            applicantSolicitorPostcode, new PostCodeModifier(applicantPostcode),
            respondentSolicitorPostcode, new PostCodeModifier(respondentPostcode),
            applicantRepresented, contestedRespondentRepresented,
            consentedRespondentRepresented
        );
    }

    private static FinremCaseData createConsentedCaseData(String applicantSolicitorPostcode, PostCodeModifier applicantPostcode,
                                                          String respondentSolicitorPostcode, PostCodeModifier respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo contestedRespondentRepresented,
                                                          YesOrNo consentedRespondentRepresented) {
        return FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantRepresented(applicantRepresented)
                .contestedRespondentRepresented(contestedRespondentRepresented)
                .consentedRespondentRepresented(consentedRespondentRepresented)
                .applicantResideOutsideUK(applicantPostcode.resideOutsideUK)
                .applicantAddress(
                    Address.builder()
                        .addressLine1("AddressLine1")
                        .addressLine2("AddressLine2")
                        .addressLine3("AddressLine3")
                        .county("County")
                        .country("Country")
                        .postTown("Town")
                        .postCode(applicantPostcode.postCode)
                        .build())
                .solicitorAddress(
                    Address.builder()
                        .addressLine1("AddressLine1")
                        .addressLine2("AddressLine2")
                        .addressLine3("AddressLine3")
                        .county("County")
                        .country("Country")
                        .postTown("Town")
                        .postCode(applicantSolicitorPostcode)
                        .build())
                .respondentResideOutsideUK(respondentPostcode.resideOutsideUK)
                .respondentAddress(
                    Address.builder()
                        .addressLine1("AddressLine1")
                        .addressLine2("AddressLine2")
                        .addressLine3("AddressLine3")
                        .county("County")
                        .country("Country")
                        .postTown("Town")
                        .postCode(respondentPostcode.postCode)
                        .build()
                )
                .respondentSolicitorAddress(
                    Address.builder()
                        .addressLine1("AddressLine1")
                        .addressLine2("AddressLine2")
                        .addressLine3("AddressLine3")
                        .county("County")
                        .country("Country")
                        .postTown("Town")
                        .postCode(respondentSolicitorPostcode)
                        .build()
                ).build()
        ).build();
    }
}
