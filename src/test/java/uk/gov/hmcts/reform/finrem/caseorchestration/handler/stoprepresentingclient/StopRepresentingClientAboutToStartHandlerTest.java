package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.StopRepresentationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.Representation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToStartHandlerTest {

    @Mock
    private StopRepresentingClientService stopRepresentingClientService;

    @InjectMocks
    private StopRepresentingClientAboutToStartHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_START, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_START, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenAsApplicantSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new Representation(TEST_USER_ID, true, false,
                false, false,
                false, false,
                false, false,
                false, false,
                -1)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Applicant)",
                "Keep the Applicant's contact details private from the Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsRespondentSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new Representation(TEST_USER_ID, false, true,
                false, false,
                false, false,
                false, false,
                false, false,
                -1)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Respondent)",
                "Keep the Respondent's contact details private from the Applicant?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsIntervenerSolicitor_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new Representation(TEST_USER_ID, false, false,
                true, false,
                false, false,
                false, false,
                false, false,
                1)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 1)",
                "Keep the Intervener 1's contact details private from the Applicant & Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsIntervenerTwoBarristerAndToRemoveIntervenerSolicitorAccess_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        Representation representation = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representation = new Representation(TEST_USER_ID, false, false,
                false, false,
                true, true,
                false, false,
                false, false,
                2)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representation))
            .thenReturn(true);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Intervener 2's contact details private from the Applicant & Respondent?");

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.YES,
                "Client's address for service (Intervener 2)",
                "Keep the Intervener 2's contact details private from the Applicant & Respondent?"
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsIntervenerTwoBarristerAndNotToRemoveIntervenerSolicitorAccess_whenHandled_thenPopulateCorrectLabel() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        Representation representation = null;
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            representation = new Representation(TEST_USER_ID, false, false,
                false, false,
                true, true,
                false, false,
                false, false,
                2)
        );
        when(stopRepresentingClientService.isIntervenerBarristerFromSameOrganisationAsSolicitor(givenFinremCaseData, representation))
            .thenReturn(false);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();

        assertThat(finremCaseData.getStopRepresentationWrapper())
            .extracting(
                StopRepresentationWrapper::getShowClientAddressForService,
                StopRepresentationWrapper::getClientAddressForServiceLabel,
                StopRepresentationWrapper::getClientAddressForServiceConfidentialLabel
            )
            .containsExactly(
                YesOrNo.NO,
                null,
                null
            );

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }

    @Test
    void givenAsCaseworker_whenHandled_thenExceptionIsThrown() {
        FinremCaseData givenFinremCaseData = FinremCaseData.builder().build();
        when(stopRepresentingClientService.buildRepresentation(givenFinremCaseData, AUTH_TOKEN)).thenReturn(
            new Representation(TEST_USER_ID, false, false,
                false, false,
                false, false,
                false, false,
                false, false,
                -1)
        );

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            givenFinremCaseData);
        assertThatThrownBy(() -> underTest.handle(callbackRequest, AUTH_TOKEN))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage(CASE_ID + " - It supports applicant/respondent representatives only");

        verify(stopRepresentingClientService).buildRepresentation(givenFinremCaseData, AUTH_TOKEN);
    }
}
