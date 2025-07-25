package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsMidHandlerTest {
    @InjectMocks
    private AmendApplicationDetailsMidHandler underTest;

    @Mock
    private InternationalPostalService internationalPostalService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_PAPER_APP_DETAILS));
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
            baseScenario(
                List.of("address error 1"),
                List.of("email error 1"),
                List.of("postal error 1"),
                List.of("address error 1", "email error 1", "postal error 1")
            ),
            baseScenario(
                List.of(),
                List.of(),
                List.of(),
                List.of()
            ),
            baseScenario(
                List.of("address only"),
                List.of(),
                List.of(),
                List.of("address only")
            )
        ).flatMap(Function.identity());
    }

    private static Stream<Arguments> baseScenario(
        List<String> addressErrors,
        List<String> emailErrors,
        List<String> postalErrors,
        List<String> expectedErrors
    ) {
        return Stream.of(
            Arguments.of(EventType.AMEND_CONTESTED_APP_DETAILS, addressErrors, emailErrors, postalErrors, expectedErrors),
            Arguments.of(EventType.AMEND_CONTESTED_PAPER_APP_DETAILS, addressErrors, emailErrors, postalErrors, expectedErrors)
        );
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void testHandleWhenExpressPilotEnabled(EventType eventType,
                                           List<String> addressErrors,
                                           List<String> emailErrors,
                                           List<String> postalErrors,
                                           List<String> expectedErrors) {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        FinremCallbackRequest callbackRequest = buildCallbackRequest(eventType, caseData, caseDataBefore);

        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
            when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);

            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataAddresses(caseData))
                .thenReturn(new ArrayList<>(addressErrors));
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataEmailAddresses(caseData))
                .thenReturn(new ArrayList<>(emailErrors));
            when(internationalPostalService.validate(caseData))
                .thenReturn(new ArrayList<>(postalErrors));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);

            verify(internationalPostalService).validate(caseData);
            verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
            verify(expressCaseService).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
        }
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void testHandleWhenExpressPilotDisabled(EventType eventType,
                                           List<String> addressErrors,
                                           List<String> emailErrors,
                                           List<String> postalErrors,
                                           List<String> expectedErrors) {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        FinremCallbackRequest callbackRequest = buildCallbackRequest(eventType, caseData, caseDataBefore);

        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
            when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);

            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataAddresses(caseData))
                .thenReturn(new ArrayList<>(addressErrors));
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataEmailAddresses(caseData))
                .thenReturn(new ArrayList<>(emailErrors));
            when(internationalPostalService.validate(caseData))
                .thenReturn(new ArrayList<>(postalErrors));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);

            verify(internationalPostalService).validate(caseData);
            verify(expressCaseService, never()).setExpressCaseEnrollmentStatus(caseData);
            verify(expressCaseService, never()).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
        }
    }

    private FinremCallbackRequest buildCallbackRequest(EventType eventType, FinremCaseData data, FinremCaseData dataBefore) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(data).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED).data(dataBefore).build())
            .build();
    }
}
