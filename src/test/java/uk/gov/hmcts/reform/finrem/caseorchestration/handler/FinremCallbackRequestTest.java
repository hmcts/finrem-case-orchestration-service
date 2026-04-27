package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;

class FinremCallbackRequestTest {
    
    @Test
    void getFinremCaseData_shouldReturnData_whenCaseDetailsPresent() {
        FinremCaseData expectedData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(expectedData).build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetails(caseDetails);

        assertThat(underTest.getFinremCaseData())
            .isSameAs(expectedData);
    }

    @Test
    void getFinremCaseData_shouldReturnNull_whenCaseDetailsIsNull() {
        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();

        assertThat(underTest.getFinremCaseData())
            .isNull();
    }

    @Test
    void getFinremCaseData_shouldReturnNull_whenDataIsNull() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetails(caseDetails);

        assertThat(underTest.getFinremCaseData())
            .isNull();
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnData_whenCaseDetailsBeforePresent() {
        FinremCaseData expectedData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(expectedData).build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetailsBefore(caseDetailsBefore);

        assertThat(underTest.getFinremCaseDataBefore())
            .isSameAs(expectedData);
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnNull_whenCaseDetailsBeforeIsNull() {
        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();

        assertThat(underTest.getFinremCaseDataBefore())
            .isNull();
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnNull_whenDataIsNull() {
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetailsBefore(caseDetailsBefore);

        assertThat(underTest.getFinremCaseDataBefore())
            .isNull();
    }

    static Stream<Arguments> provideDifferentEmailAddresses() {
        return Stream.of(
            Arguments.of(TEST_SOLICITOR_EMAIL, "def@def.com"),
            Arguments.of(TEST_SOLICITOR_EMAIL, null),
            Arguments.of(TEST_SOLICITOR_EMAIL, ""),
            Arguments.of("def@def.com", TEST_SOLICITOR_EMAIL),
            Arguments.of(null, TEST_SOLICITOR_EMAIL),
            Arguments.of("", TEST_SOLICITOR_EMAIL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDifferentEmailAddresses")
    void shouldReturnTrueWhenApplicantSolicitorEmailIsDifferent(String beforeEmail, String afterEmail) {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(beforeEmail);
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(afterEmail);

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertTrue(underTest.isApplicantSolicitorChanged());
    }

    @ParameterizedTest
    @MethodSource("provideDifferentEmailAddresses")
    void shouldReturnTrueWhenRespondentSolicitorEmailIsDifferent(String beforeEmail, String afterEmail) {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn(beforeEmail);
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(afterEmail);

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertTrue(underTest.isRespondentSolicitorChanged());
    }

    private static Stream<Arguments> changedPolicyProvider() {
        return Stream.of(
            Arguments.of("TEST_ORG_ID", "TEST_ORG2_ID"),
            Arguments.of(null, "TEST_ORG2_ID"),
            Arguments.of("TEST_ORG_ID", null)
        );
    }

    @ParameterizedTest
    @MethodSource("changedPolicyProvider")
    void shouldReturnTrueWhenApplicantOrganisationPoliciesChanged(String beforeOrganisationPolicyId, String afterOrganisationPolicyId) {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(afterOrganisationPolicyId));
        when(finremCaseDataBefore.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(beforeOrganisationPolicyId));

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder().data(finremCaseData).build())
            .caseDetailsBefore(FinremCaseDetails.builder().data(finremCaseDataBefore).build()).build();
        assertTrue(underTest.isApplicantSolicitorChanged());
    }

    @ParameterizedTest
    @MethodSource("changedPolicyProvider")
    void shouldReturnTrueWhenRespondentOrganisationPoliciesChanged(String beforeOrganisationPolicyId, String afterOrganisationPolicyId) {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(afterOrganisationPolicyId));
        when(finremCaseDataBefore.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(beforeOrganisationPolicyId));

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder().data(finremCaseData).build())
            .caseDetailsBefore(FinremCaseDetails.builder().data(finremCaseDataBefore).build()).build();
        assertTrue(underTest.isRespondentSolicitorChanged());
    }

    @Test
    void shouldReturnFalseWhenBothApplicantOrganisationPoliciesAreMissing() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertFalse(underTest.isApplicantSolicitorChanged());
    }

    @Test
    void shouldReturnFalseWhenBothRespondentOrganisationPoliciesAreMissing() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertFalse(underTest.isRespondentSolicitorChanged());
    }

    @Test
    void shouldReturnFalseWhenApplicantPolicyAndEmailAreIdentical() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));
        when(finremCaseDataBefore.getApplicantOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertFalse(underTest.isApplicantSolicitorChanged());
    }

    @Test
    void shouldReturnFalseWhenRespondentPolicyAndEmailAreIdentical() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseDataBefore = mock(FinremCaseData.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetails.builder().data(finremCaseDataBefore).build();

        when(finremCaseData.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));
        when(finremCaseDataBefore.getRespondentOrganisationPolicy()).thenReturn(organisationPolicy(TEST_ORG_ID));

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertFalse(underTest.isRespondentSolicitorChanged());
    }
}
