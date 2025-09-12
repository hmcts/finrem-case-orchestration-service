package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import java.util.List;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;

class RespondentSolicitorDetailsValidatorTest {

    private final RespondentSolicitorDetailsValidator underTest = new RespondentSolicitorDetailsValidator();

    @Test
    void givenConsentedRespondentNotRepresented_whenValidate_thenReturnEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    @Test
    void givenContestedRespondentNotRepresented_whenValidate_thenReturnEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    static Stream<OrganisationPolicy> givenRespondentRepresentedAndOrganisationPolicyMissing_whenValidate_thenReturnAnError() {
        return Stream.of(
            null,
            OrganisationPolicy.builder().build(),
            OrganisationPolicy.builder().organisation(Organisation.builder().build()).build()
        );
    }

    @ParameterizedTest
    @MethodSource
    void givenRespondentRepresentedAndOrganisationPolicyMissing_whenValidate_thenReturnAnError(
        OrganisationPolicy organisationPolicy
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        finremCaseData.setRespondentOrganisationPolicy(organisationPolicy);
        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).containsExactly("Respondent organisation policy is missing.");
    }

    @Test
    void givenRespondentRepresentedAndValidOrganisationPolicy_whenValidate_thenEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        finremCaseData.setRespondentOrganisationPolicy(validOrganisationPolicy());

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    private static OrganisationPolicy validOrganisationPolicy() {
        return OrganisationPolicy.builder().organisation(Organisation.builder()
            .organisationID(TEST_ORG_ID).build()).build();
    }

    @ParameterizedTest
    @MethodSource
    void givenRepresentedRespondentAndInvalidSolicitorAddress_whenValidate_thenReturnAnError(
        Address respondentSolicitorAddress
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(respondentSolicitorAddress);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).contains("Respondent solicitor's address is required.");
    }

    private static Stream<Arguments> givenRepresentedRespondentAndInvalidSolicitorAddress_whenValidate_thenReturnAnError() {
        return Stream.of(
            Arguments.of((Address) null),
            Arguments.of(Address.builder().build())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidStringValues")
    void givenRepresentedRespondentAndInvalidSolicitorEmail_whenValidate_thenReturnEmailError(
        String email
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(validAddress());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorEmail(email);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).anyMatch(error -> error.contains("email"));
    }

    @ParameterizedTest
    @MethodSource("invalidStringValues")
    void givenRepresentedRespondentAndInvalidPhone_whenValidate_thenReturnPhoneError(
        String phone
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(validAddress());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorPhone(phone);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).anyMatch(error -> error.contains("phone"));
    }

    @ParameterizedTest
    @MethodSource("invalidStringValues")
    void givenRepresentedRespondentAndInvalidFirm_whenValidate_thenReturnFirmError(
        String firm
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(validAddress());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorFirm(firm);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).anyMatch(error -> error.contains("name of firm"));
    }

    @ParameterizedTest
    @MethodSource("invalidStringValues")
    void givenRepresentedRespondentAndInvalidName_whenValidate_thenReturnNameError(
        String name
    ) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.isConsentedApplication()).thenReturn(true);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(validAddress());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorName(name);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).anyMatch(error -> error.contains("name"));
    }

    private static Stream<Arguments> invalidStringValues() {
        return Stream.of(
            Arguments.of((String) null),
            Arguments.of(""),
            Arguments.of("   ")
        );
    }

    @Test
    void givenRepresentedAndValidSolicitorDetails_whenValidate_thenEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
        finremCaseData.setRespondentOrganisationPolicy(validOrganisationPolicy());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(validAddress());
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorEmail("dasd@gmail.com");
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorPhone("01234567890");
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorFirm("Solicitor Firm");
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorName("Alexander Isak");

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    @Test
    void givenNotRepresented_whenValidate_thenEmptyList() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorEmail(null);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorPhone(null);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorFirm(null);
        finremCaseData.getContactDetailsWrapper().setRespondentSolicitorName(null);

        List<String> actual = underTest.validate(finremCaseData);
        assertThat(actual).isEmpty();
    }

    private Address validAddress() {
        return Address.builder()
            .addressLine1("20 Cromie Close")
            .postTown("London")
            .postCode("N13 4BF")
            .build();
    }
}