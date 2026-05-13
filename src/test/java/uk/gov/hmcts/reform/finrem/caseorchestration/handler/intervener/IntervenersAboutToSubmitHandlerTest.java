package uk.gov.hmcts.reform.finrem.caseorchestration.handler.intervener;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener.IntervenerCoversheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.intervener.IntervenerService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.ADD_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_FOUR_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_ONE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_THREE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerConstant.DEL_INTERVENER_TWO_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IntervenersAboutToSubmitHandlerTest {

    @InjectMocks
    private IntervenersAboutToSubmitHandler aboutToSubmitHandler;

    @Mock
    private IntervenerService intervenerService;

    @Mock
    private IntervenerCoversheetService intervenerCoversheetService;

    @Test
    void testCanHandle() {
        assertCanHandle(
            aboutToSubmitHandler,
            CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED,
            EventType.MANAGE_INTERVENERS
        );
    }

    @Nested
    class AddIntervenerTests {
        @Test
        void givenAddIntervenerOneAndValidationPasses_whenHandle_thenValidateAndUpdateIntervenerDetailsIsCalled() {
            FinremCallbackRequest callbackRequest = getValidFinremCallbackRequest(ADD_INTERVENER_ONE_CODE);
            FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

            IntervenerChangeDetails changeDetails = mock(IntervenerChangeDetails.class);
            IntervenerOne intervener = mock(IntervenerOne.class);
            when(caseData.getIntervenerOne()).thenReturn(intervener);
            when(intervenerService.updateIntervenerDetails(intervener, new ArrayList<>(), callbackRequest)).thenReturn(changeDetails);

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

            assertAll(
                () -> verify(intervenerService).validateIntervenerInformation(intervener, response.getErrors()),
                () -> verify(intervenerService).updateIntervenerDetails(intervener, response.getErrors(), callbackRequest),
                () -> verify(intervenerCoversheetService).updateIntervenerCoversheet(callbackRequest.getCaseDetails(), changeDetails, AUTH_TOKEN),

                () -> assertThat(response.getErrors()).isEmpty(),
                () -> assertThat(response.getData()).isEqualTo(caseData)
            );
        }

        @Test
        void givenAddIntervenerOneAndValidationReturnsError_whenHandle_thenDoesNotUpdateIntervenerDetails() {
            FinremCallbackRequest callbackRequest = getValidFinremCallbackRequest(ADD_INTERVENER_ONE_CODE);
            FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

            IntervenerOne intervener = mock(IntervenerOne.class);
            when(caseData.getIntervenerOne()).thenReturn(intervener);

            doAnswer(invocation -> {
                List<String> errors = invocation.getArgument(1);
                errors.add("Validation error");
                return null;
            }).when(intervenerService).validateIntervenerInformation(eq(intervener), anyList());

            GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
                aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getErrors()).containsExactly("Validation error");

            verify(intervenerService).validateIntervenerInformation(intervener, response.getErrors());
            verify(intervenerService, never()).updateIntervenerDetails(any(), any(), any());
            verify(intervenerService, never()).removeIntervenerDetails(any(), any(), any(), any());
        }
    }

    @Test
    void givenDeleteIntervenerOne_whenHandle_thenRemoveIntervenerDetailsIsCalled() {
        FinremCallbackRequest callbackRequest = getValidFinremCallbackRequest(DEL_INTERVENER_ONE_CODE);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        IntervenerOne intervener = mock(IntervenerOne.class);
        when(caseData.getIntervenerOne()).thenReturn(intervener);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).isEqualTo(caseData);

        verify(intervenerService).removeIntervenerDetails(
            intervener,
            response.getErrors(),
            caseData,
            Long.valueOf(CASE_ID)
        );

        verify(intervenerService, never()).validateIntervenerInformation(any(), any());
        verify(intervenerService, never()).updateIntervenerDetails(any(), any(), any());
    }

    @Test
    void givenInvalidOperationCode_whenHandle_thenThrowsIllegalArgumentException() {
        FinremCallbackRequest callbackRequest = getValidFinremCallbackRequest("invalid-code");

        assertThatThrownBy(() -> aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid operation code: invalid-code");

        verifyNoInteractions(intervenerService);
    }

    @ParameterizedTest
    @MethodSource("deleteIntervenerOperationCodes")
    void givenDeleteIntervenerOperationCode_whenHandle_thenCorrectIntervenerIsRemoved(
        String operationCode,
        int intervenerNumber
    ) {
        FinremCallbackRequest callbackRequest = getValidFinremCallbackRequest(operationCode);
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        IntervenerWrapper expectedIntervener = mockIntervener(caseData, intervenerNumber);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            aboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();

        verify(intervenerService).removeIntervenerDetails(
            expectedIntervener,
            response.getErrors(),
            caseData,
            Long.valueOf(CASE_ID)
        );

        verify(intervenerService, never()).validateIntervenerInformation(any(), any());
        verify(intervenerService, never()).updateIntervenerDetails(any(), any(), any());
    }

    private static Stream<Arguments> deleteIntervenerOperationCodes() {
        return Stream.of(
            arguments(DEL_INTERVENER_ONE_CODE, 1),
            arguments(DEL_INTERVENER_TWO_CODE, 2),
            arguments(DEL_INTERVENER_THREE_CODE, 3),
            arguments(DEL_INTERVENER_FOUR_CODE, 4)
        );
    }

    private IntervenerWrapper mockIntervener(FinremCaseData caseData, int intervenerNumber) {
        return switch (intervenerNumber) {
            case 1 -> {
                IntervenerOne intervener = mock(IntervenerOne.class);
                when(caseData.getIntervenerOne()).thenReturn(intervener);
                yield intervener;
            }
            case 2 -> {
                IntervenerTwo intervener = mock(IntervenerTwo.class);
                when(caseData.getIntervenerTwo()).thenReturn(intervener);
                yield intervener;
            }
            case 3 -> {
                IntervenerThree intervener = mock(IntervenerThree.class);
                when(caseData.getIntervenerThree()).thenReturn(intervener);
                yield intervener;
            }
            case 4 -> {
                IntervenerFour intervener = mock(IntervenerFour.class);
                when(caseData.getIntervenerFour()).thenReturn(intervener);
                yield intervener;
            }
            default -> throw new IllegalArgumentException("Invalid intervener number: " + intervenerNumber);
        };
    }

    private FinremCallbackRequest getValidFinremCallbackRequest(String selectedOperationCode) {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class, RETURNS_DEEP_STUBS);
        FinremCaseData caseData = mock(FinremCaseData.class, RETURNS_DEEP_STUBS);

        when(callbackRequest.getCaseDetails().getId()).thenReturn(Long.valueOf(CASE_ID));
        when(callbackRequest.getCaseDetails().getData()).thenReturn(caseData);
        when(caseData.getIntervenerOptionList().getValueCode()).thenReturn(selectedOperationCode);

        return callbackRequest;
    }
}
