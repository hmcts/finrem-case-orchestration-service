package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateConsentedMidHandlerTest {


    @InjectMocks
    private SolicitorCreateConsentedMidHandler underTest;
    @Mock
    private ConsentOrderService consentOrderService;
    @Mock
    private InternationalPostalService internationalPostalService;
    @Spy
    private ObjectMapper objectMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.MID_EVENT, CONSENTED, EventType.SOLICITOR_CREATE);
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
            Arguments.of(
                List.of("address error 1"),
                List.of("email error 1"),
                List.of("postal error 1"),
                List.of("consent check error"),
                List.of("consent check error", "postal error 1", "address error 1", "email error 1")
            ),
            Arguments.of(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
            ),
            Arguments.of(
                List.of("address only"),
                List.of(),
                List.of(),
                List.of(),
                List.of("address only")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void testHandle(List<String> addressErrors,
                    List<String> emailErrors,
                    List<String> postalErrors,
                    List<String> consentErrors,
                    List<String> expectedErrors) {

        FinremCaseData caseData = FinremCaseData.builder().ccdCaseType(CONSENTED).build();
        FinremCallbackRequest callbackRequest = buildCallbackRequest(caseData);

        try (MockedStatic<ContactDetailsValidator> contactValidatorMock = mockStatic(ContactDetailsValidator.class)) {
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataAddresses(caseData))
                .thenReturn(new ArrayList<>(addressErrors));
            contactValidatorMock.when(() -> ContactDetailsValidator.validateCaseDataEmailAddresses(caseData))
                .thenReturn(new ArrayList<>(emailErrors));
            when(consentOrderService.performCheck(any(CallbackRequest.class), eq(AUTH_TOKEN)))
                .thenReturn(new ArrayList<>(consentErrors));
            when(internationalPostalService.validate(caseData))
                .thenReturn(new ArrayList<>(postalErrors));

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactlyElementsOf(expectedErrors);

            verify(internationalPostalService).validate(caseData);
            verify(consentOrderService).performCheck(any(CallbackRequest.class), eq(AUTH_TOKEN));
        }
    }

    private FinremCallbackRequest buildCallbackRequest(FinremCaseData data) {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(data).build())
            .build();
    }
}
