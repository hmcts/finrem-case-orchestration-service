package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    // TODO
//    if (caseData.isConsentedApplication()) {
//        if (contactDetailsWrapper.getSolicitorAddress() == null
//            || !NullChecker.anyNonNull(contactDetailsWrapper.getSolicitorAddress())) {
//            ret.add("Applicant solicitor's address is required.");
//        }
//        validateField(contactDetailsWrapper.getSolicitorEmail(), "email", ret);
//        validateField(contactDetailsWrapper.getSolicitorPhone(), "phone", ret);
//        validateField(contactDetailsWrapper.getSolicitorFirm(), "name of your firm", ret);
//        validateField(contactDetailsWrapper.getSolicitorName(), "name", ret);
//    }

    private static OrganisationPolicy validOrganisationPolicy() {
        return OrganisationPolicy.builder().organisation(Organisation.builder()
            .organisationID(TEST_ORG_ID).build()).build();
    }
}
