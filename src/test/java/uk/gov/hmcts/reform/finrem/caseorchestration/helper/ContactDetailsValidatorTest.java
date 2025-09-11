package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.APPLICANT_SOLICITOR_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_POSTCODE_ERROR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator.RESPONDENT_SOLICITOR_POSTCODE_ERROR;

class ContactDetailsValidatorTest {

    private static final String INVALID_EMAIL = "invalid-email";
    private static final String VALID_EMAIL = "valid@email.com";
    private static final String ERROR_MSG = "%s is not a valid Email address.";
    private static final String DIFFERENT_ORG_ID = "ORG456";

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

    @Test
    void givenBothOrganisationIdsNull_whenValidated_thenNoErrorIsReturned() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        OrganisationPolicy applicantPolicy = mockOrganisationPolicy(null);
        OrganisationPolicy respondentPolicy = mockOrganisationPolicy(null);

        when(caseData.getApplicantOrganisationPolicy()).thenReturn(applicantPolicy);
        when(caseData.getRespondentOrganisationPolicy()).thenReturn(respondentPolicy);

        List<String> errors = ContactDetailsValidator.validateOrganisationPolicy(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void givenBothOrganisationPolicyNull_whenValidated_thenNoErrorIsReturned() {
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseData.getApplicantOrganisationPolicy()).thenReturn(null);
        when(caseData.getRespondentOrganisationPolicy()).thenReturn(null);

        List<String> errors = ContactDetailsValidator.validateOrganisationPolicy(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void givenEqualOrganisationIds_whenValidated_thenErrorIsReturned() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        OrganisationPolicy applicantPolicy = mockOrganisationPolicy(TEST_ORG_ID);
        OrganisationPolicy respondentPolicy = mockOrganisationPolicy(TEST_ORG_ID);

        when(caseData.getApplicantOrganisationPolicy()).thenReturn(applicantPolicy);
        when(caseData.getRespondentOrganisationPolicy()).thenReturn(respondentPolicy);

        List<String> errors = ContactDetailsValidator.validateOrganisationPolicy(caseData);

        assertThat(errors).containsExactly("Solicitor can only represent one party.");
    }

    @Test
    void givenDistinctOrganisationIds_whenValidated_thenNoErrorIsReturned() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        OrganisationPolicy applicantPolicy = mockOrganisationPolicy(TEST_ORG_ID);
        OrganisationPolicy respondentPolicy = mockOrganisationPolicy(DIFFERENT_ORG_ID);

        when(caseData.getApplicantOrganisationPolicy()).thenReturn(applicantPolicy);
        when(caseData.getRespondentOrganisationPolicy()).thenReturn(respondentPolicy);

        List<String> errors = ContactDetailsValidator.validateOrganisationPolicy(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void givenOneOrganisationIsNull_whenValidated_thenNoErrorIsReturned() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        OrganisationPolicy applicantPolicy = mockOrganisationPolicy(null);
        OrganisationPolicy respondentPolicy = mockOrganisationPolicy(TEST_ORG_ID);

        when(caseData.getApplicantOrganisationPolicy()).thenReturn(applicantPolicy);
        when(caseData.getRespondentOrganisationPolicy()).thenReturn(respondentPolicy);

        List<String> errors = ContactDetailsValidator.validateOrganisationPolicy(caseData);

        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @MethodSource
    void shouldValidatePostcodeConditionsAndReturnExpectedErrors(FinremCaseData caseData, String expectedError) {
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        if (expectedError != null) {
            assertThat(errors).containsOnly(expectedError);
        } else {
            assertThat(errors).isEmpty();
        }
    }

    private static Stream<Object[]> shouldValidatePostcodeConditionsAndReturnExpectedErrors() {
        return Stream.of(
            new Object[] {
                createConsentedCaseData(null, new PostCodeModifier("SW1A 1AA"), "E1 6AN", new PostCodeModifier("EC1A 1BB"), YesOrNo.YES, null),
                APPLICANT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(null, false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(" ", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(null, true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier(" ", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, YesOrNo.YES),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, YesOrNo.YES),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(""), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" "), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier("", true), null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" ", true), null, null),
                null },
            new Object[] {
                createConsentedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null, true), null, null),
                null },
            // Contested
            new Object[] {
                createContestedCaseData(null, new PostCodeModifier("SW1A 1AA"), "E1 6AN", new PostCodeModifier("EC1A 1BB"), YesOrNo.YES, null),
                APPLICANT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier(null, false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier(" ", false), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                APPLICANT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier(null, true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier(" ", true), "E1 6AN", new PostCodeModifier("EC1A 1BB"), null, null),
                null },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, YesOrNo.YES),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), null, new PostCodeModifier("EC1A 1BB"), null, YesOrNo.YES),
                RESPONDENT_SOLICITOR_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(""), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" "), null, null),
                RESPONDENT_POSTCODE_ERROR },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier("", true), null, null),
                null },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(" ", true), null, null),
                null },
            new Object[] {
                createContestedCaseData("SW1A 1AA", new PostCodeModifier("E1 6AN"), "EC1A 1BB", new PostCodeModifier(null, true), null, null),
                null }
        );
    }

    @Test
    void shouldReturnNoErrorsForValidCaseData() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null);
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenApplicantAddressIsEmpty() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null);
        caseData.getContactDetailsWrapper().setApplicantAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenRespondentAddressIsEmpty() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null);
        caseData.getContactDetailsWrapper().setRespondentAddress(new Address()); // Empty address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenApplicantAddressIsNull() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", null, "E1 6AN", "EC1A 1BB", null, null);
        caseData.getContactDetailsWrapper().setApplicantAddress(null); // Null address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorWhenRespondentAddressIsNull() {
        FinremCaseData caseData = createConsentedCaseData("SW1A 1AA", "E1 6AN", "EC1A 1BB", "B1 1BB", null, null);
        caseData.getContactDetailsWrapper().setRespondentAddress(null); // Null address object
        List<String> errors = ContactDetailsValidator.validateCaseDataAddresses(caseData);
        assertThat(errors).isEmpty();
    }

    private static FinremCaseData createContestedCaseData(String applicantSolicitorPostcode, String applicantPostcode,
                                                          String respondentSolicitorPostcode, String respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo respondentRepresented) {
        return createContestedCaseData(
            applicantSolicitorPostcode, new PostCodeModifier(applicantPostcode),
            respondentSolicitorPostcode, new PostCodeModifier(respondentPostcode),
            applicantRepresented, respondentRepresented
        );
    }

    private static FinremCaseData createContestedCaseData(String applicantSolicitorPostcode, PostCodeModifier applicantPostcode,
                                                          String respondentSolicitorPostcode, PostCodeModifier respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo respondentRepresented) {
        return FinremCaseData.builder().ccdCaseType(CaseType.CONTESTED).contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantRepresented(applicantRepresented)
                .contestedRespondentRepresented(respondentRepresented)
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
                .applicantSolicitorAddress(
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

    private static FinremCaseData createConsentedCaseData(String applicantSolicitorPostcode, String applicantPostcode,
                                                          String respondentSolicitorPostcode, String respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo respondentRepresented) {
        return createConsentedCaseData(
            applicantSolicitorPostcode, new PostCodeModifier(applicantPostcode),
            respondentSolicitorPostcode, new PostCodeModifier(respondentPostcode),
            applicantRepresented, respondentRepresented
        );
    }

    private static FinremCaseData createConsentedCaseData(String applicantSolicitorPostcode, PostCodeModifier applicantPostcode,
                                                          String respondentSolicitorPostcode, PostCodeModifier respondentPostcode,
                                                          YesOrNo applicantRepresented, YesOrNo respondentRepresented) {
        return FinremCaseData.builder().ccdCaseType(CaseType.CONSENTED).contactDetailsWrapper(
            ContactDetailsWrapper.builder()
                .applicantRepresented(applicantRepresented)
                .consentedRespondentRepresented(respondentRepresented)
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

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideCaseDataScenarios")
    @DisplayName("Email validation test cases")
    void shouldValidateEmailAddresses(String description, FinremCaseData caseData, List<String> expectedErrors) {
        List<String> errors = ContactDetailsValidator.validateCaseDataEmailAddresses(caseData);
        assertEquals(expectedErrors, errors);
    }

    private static Stream<Arguments> provideCaseDataScenarios() {
        return Stream.of(
            // Invalid applicant solicitor email - Contested
            Arguments.of(
                "Invalid applicant solicitor email (Contested)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONTESTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .applicantSolicitorEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // Invalid applicant solicitor email - Consented
            Arguments.of(
                "Invalid applicant solicitor email (Consented)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONSENTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .solicitorEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // Invalid applicant email
            Arguments.of(
                "Invalid applicant email",
                FinremCaseData.builder()
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // Invalid respondent solicitor email
            Arguments.of(
                "Invalid respondent solicitor email (Contested)",
                FinremCaseData.builder()
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .contestedRespondentRepresented(YesOrNo.YES)
                        .respondentSolicitorEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // Invalid respondent solicitor email
            Arguments.of(
                "Invalid respondent solicitor email (Consented)",
                FinremCaseData.builder()
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .consentedRespondentRepresented(YesOrNo.YES)
                        .respondentSolicitorEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // Invalid respondent email (not represented)
            Arguments.of(
                "Invalid respondent email (not represented, Contested)",
                FinremCaseData.builder()
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .contestedRespondentRepresented(YesOrNo.NO)
                        .respondentEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),
            Arguments.of(
                "Invalid respondent email (not represented, Consented)",
                FinremCaseData.builder()
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .consentedRespondentRepresented(YesOrNo.NO)
                        .respondentEmail(INVALID_EMAIL)
                        .build())
                    .build(),
                List.of(String.format(ERROR_MSG, INVALID_EMAIL))
            ),

            // All valid emails
            Arguments.of(
                "All valid emails (Contested)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONTESTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .contestedRespondentRepresented(YesOrNo.YES)
                        .applicantSolicitorEmail(VALID_EMAIL)
                        .applicantEmail(VALID_EMAIL)
                        .respondentSolicitorEmail(VALID_EMAIL)
                        .respondentEmail(VALID_EMAIL)
                        .build())
                    .build(),
                List.of()
            ),

            Arguments.of(
                "All valid emails (Consented)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONSENTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .consentedRespondentRepresented(YesOrNo.YES)
                        .solicitorEmail(VALID_EMAIL)
                        .applicantEmail(VALID_EMAIL)
                        .respondentSolicitorEmail(VALID_EMAIL)
                        .respondentEmail(VALID_EMAIL)
                        .build())
                    .build(),
                List.of()
            ),

            Arguments.of(
                "All valid emails - optional respondent solicitor email (Contested)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONTESTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .contestedRespondentRepresented(YesOrNo.YES)
                        .applicantSolicitorEmail(VALID_EMAIL)
                        .applicantEmail(VALID_EMAIL)
                        .respondentSolicitorEmail("")
                        .respondentEmail(VALID_EMAIL)
                        .build())
                    .build(),
                List.of()
            ),

            Arguments.of(
                "All valid emails - optional respondent solicitor email (Consented)",
                FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONSENTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantRepresented(YesOrNo.YES)
                        .consentedRespondentRepresented(YesOrNo.YES)
                        .solicitorEmail(VALID_EMAIL)
                        .applicantEmail(VALID_EMAIL)
                        .respondentSolicitorEmail(null)
                        .respondentEmail(VALID_EMAIL)
                        .build())
                    .build(),
                List.of()
            )
        );
    }

    private OrganisationPolicy mockOrganisationPolicy(String organisationId) {
        Organisation organisation = mock(Organisation.class);
        when(organisation.getOrganisationID()).thenReturn(organisationId);

        OrganisationPolicy policy = mock(OrganisationPolicy.class);
        when(policy.getOrganisation()).thenReturn(organisation);

        return policy;
    }
}
