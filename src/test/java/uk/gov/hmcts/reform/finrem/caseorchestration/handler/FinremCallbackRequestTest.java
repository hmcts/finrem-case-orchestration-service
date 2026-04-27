package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;

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

        when(finremCaseDataBefore.getAppSolicitorEmailIfRepresented()).thenReturn(beforeEmail);
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

        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(beforeEmail);
        when(finremCaseDataBefore.getRespSolicitorEmailIfRepresented()).thenReturn(afterEmail);

        FinremCallbackRequest underTest = FinremCallbackRequest.builder()
            .caseDetails(finremCaseDetails)
            .caseDetailsBefore(finremCaseDetailsBefore)
            .build();
        assertTrue(underTest.isRespondentSolicitorChanged());
    }
}
