package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.NullChecker;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;

class ApplicantSolicitorDetailsValidatorTest {

    private final ApplicantSolicitorDetailsValidator underTest = new ApplicantSolicitorDetailsValidator();

    @Test
    void givenApplicantNotRepresented_whenValidate_thenReturnEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    static Stream<OrganisationPolicy> givenApplicantRepresentedAndOrganisationPolicyMissing_whenValidate_thenReturnAnError() {
        return Stream.of(
            null,
            OrganisationPolicy.builder().build(),
            OrganisationPolicy.builder().organisation(Organisation.builder().build()).build()
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenApplicantRepresentedAndOrganisationPolicyMissing_whenValidate_thenReturnAnError(
        OrganisationPolicy organisationPolicy
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.setApplicantOrganisationPolicy(organisationPolicy);


        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).containsExactly("Applicant organisation policy is missing.");
    }

    @Test
    void givenApplicantRepresentedAndValidOrganisationPolicy_whenValidate_thenEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.setApplicantOrganisationPolicy(validOrganisationPolicy());

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    @Test
    void givenContestedCase_whenValidate_thenReturnEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        finremCaseData.setApplicantOrganisationPolicy(validOrganisationPolicy());
        when(finremCaseData.isConsentedInContestedCase()).thenReturn(false);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    static Stream<Arguments> givenConsentedCaseAndSolicitorMissing_whenValidate_thenReturnAnError() {
        return Stream.of(
            Arguments.of(Address.builder().build())
        );
    }

    @ParameterizedTest
    @MethodSource
    @NullSource
    void givenConsentedCaseAndSolicitorMissing_whenValidate_thenReturnAnError(Address solicitorAddress) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(validOrganisationPolicy());
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setSolicitorAddress(solicitorAddress);
        if (solicitorAddress != null) {
            try (MockedStatic<NullChecker> mockedStatic = mockStatic(NullChecker.class)) {
                mockedStatic.when(() -> NullChecker.anyNonNull(solicitorAddress))
                    .thenReturn(false);

                List<String> actual = underTest.validate(finremCaseData);
                assertThat(actual).contains("Applicant solicitor's address is required.");
                mockedStatic.verify(() -> NullChecker.anyNonNull(solicitorAddress));
            }
        } else {
            List<String> actual = underTest.validate(finremCaseData);
            assertThat(actual).contains("Applicant solicitor's address is required.");
        }
    }

    static Stream<Arguments> givenConsentedCaseAndContactDetailsWrapper_whenValidate_thenReturnErrors() {
        final String messageTpl = "Applicant solicitor's %s is required.";
        final String missingAddressMessage = "Applicant solicitor's address is required.";
        return Stream.of(
            Arguments.of(
                ContactDetailsWrapper.builder()
                    .solicitorEmail(null)
                    .solicitorPhone("07788666444")
                    .solicitorFirm("ABC Co Ltd")
                    .solicitorName("A Cole")
                    .build(),
                List.of(format(messageTpl, "email"), missingAddressMessage)
            ),
            Arguments.of(
                ContactDetailsWrapper.builder()
                    .solicitorEmail("abc@abc.com")
                    .solicitorPhone(null)
                    .solicitorFirm("ABC Co Ltd")
                    .solicitorName("A Cole")
                    .build(),
                List.of(format(messageTpl, "phone"), missingAddressMessage)
            ),
            Arguments.of(
                ContactDetailsWrapper.builder()
                    .solicitorEmail(null)
                    .solicitorPhone(null)
                    .solicitorFirm("ABC Co Ltd")
                    .solicitorName("A Cole")
                    .build(),
                List.of(format(messageTpl, "phone"), format(messageTpl, "email"), missingAddressMessage)
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenConsentedCaseAndContactDetailsWrapper_whenValidate_thenReturnErrors(
        ContactDetailsWrapper contactDetailsWrapper, List<String> expectedErrors) {

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(validOrganisationPolicy());
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.setContactDetailsWrapper(contactDetailsWrapper);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).contains(expectedErrors.toArray(new String[0]));
    }

    private static OrganisationPolicy validOrganisationPolicy() {
        return OrganisationPolicy.builder().organisation(Organisation.builder()
            .organisationID(TEST_ORG_ID).build()).build();
    }
}
