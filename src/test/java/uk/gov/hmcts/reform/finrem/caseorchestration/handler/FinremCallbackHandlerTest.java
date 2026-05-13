package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;

@ExtendWith(MockitoExtension.class)
class FinremCallbackHandlerTest {

    static class GeneralFinremCallbackHandler extends FinremCallbackHandler {

        public GeneralFinremCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData());
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    static class ValidateCaseDataTestHandler extends GeneralFinremCallbackHandler {
        public ValidateCaseDataTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            validateCaseData(callbackRequest);
            return response(callbackRequest.getCaseDetails().getData());
        }
    }

    static class GeneralAboutToSubmitCallbackHandler extends FinremAboutToSubmitCallbackHandler {

        public GeneralAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData());
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    static class ResponseWithoutWarningsTestHandler extends GeneralAboutToSubmitCallbackHandler {

        public ResponseWithoutWarningsTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return responseWithoutWarnings(callbackRequest.getCaseDetails().getData(),
                List.of("ERROR12345"));
        }
    }

    static class ResponseTestHandler extends GeneralAboutToSubmitCallbackHandler {

        public ResponseTestHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return response(callbackRequest.getCaseDetails().getData(),
                List.of("WARNING_1111"),
                List.of("ERROR_1111"),
                "POST_STATE");
        }
    }

    static class GeneralSubmittedCallbackHandler extends FinremCallbackHandler {

        public GeneralSubmittedCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return submittedResponse(toConfirmationHeader("TEST"),
                toConfirmationBody("Message One", "Message Two"));
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private GeneralFinremCallbackHandler generalFinremCallbackHandler;
    private GeneralAboutToSubmitCallbackHandler generalAboutToSubmitCallbackHandler;
    private GeneralSubmittedCallbackHandler generalSubmittedCallbackHandler;
    private ResponseWithoutWarningsTestHandler responseWithoutWarningsTestHandler;
    private ResponseTestHandler responseTestHandler;
    private ValidateCaseDataTestHandler validateCaseDataTestHandler;

    private static final Map<String, Object> TESTING_DATA_IN_MAP = Map.of(
        "retained", YesOrNo.YES,
        "stopRepJudicialApproval", YesOrNo.YES,
        "clientAddressForService", Address.builder().addressLine1("Test Address").build()
    );

    @BeforeEach
    void setUp() {
        generalFinremCallbackHandler = spy(new GeneralFinremCallbackHandler(finremCaseDetailsMapper));
        generalAboutToSubmitCallbackHandler = spy(new GeneralAboutToSubmitCallbackHandler(finremCaseDetailsMapper));
        generalSubmittedCallbackHandler = spy(new GeneralSubmittedCallbackHandler(finremCaseDetailsMapper));
        responseWithoutWarningsTestHandler = new ResponseWithoutWarningsTestHandler(finremCaseDetailsMapper);
        responseTestHandler = new ResponseTestHandler(finremCaseDetailsMapper);
        validateCaseDataTestHandler = new ValidateCaseDataTestHandler(finremCaseDetailsMapper);
    }

    @Test
    void shouldPopulateCaseIdToBothFinremCaseDetails() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(finremCaseDetails.getData()).thenReturn(caseData);
        when(finremCaseDetails.getCaseIdAsString()).thenReturn(CASE_ID);

        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(finremCaseDetailsBefore.getData()).thenReturn(caseDataBefore);
        CallbackRequest callbackRequest = mock(CallbackRequest.class);
        when(callbackRequest.getEventId()).thenReturn("FR_sendOrder");

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        CaseDetails caseDetailsBefore = mock(CaseDetails.class);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)).thenReturn(finremCaseDetails);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetailsBefore)).thenReturn(finremCaseDetailsBefore);

        generalFinremCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(caseData).setCcdCaseId(CASE_ID),
            () -> verify(caseDataBefore).setCcdCaseId(CASE_ID)
        );
    }

    @Test
    void givenAboutToSubmitCallbackHandler_whenHandled_thenTemporaryFieldsRemoved() {
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(STOP_REPRESENTING_CLIENT.getCcdType())
            .caseDetails(mock(CaseDetails.class))
            .build();

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails()))
            .thenReturn(finremCaseDetails);
        FinremCaseData responseData = mock(FinremCaseData.class);
        when(finremCaseDetails.getData()).thenReturn(responseData);
        CaseDetails toBeSanitisedCaseDetails = CaseDetails.builder().data(new HashMap<>(TESTING_DATA_IN_MAP)).build();
        when(finremCaseDetailsMapper.mapToCaseDetails(
            argThat(arg -> responseData.equals(arg.getData())))
        ).thenReturn(toBeSanitisedCaseDetails);
        FinremCaseData expectedResponseData = mock(FinremCaseData.class);
        when(finremCaseDetailsMapper.mapToFinremCaseData(Map.of("retained", YesOrNo.YES)))
            .thenReturn(expectedResponseData);

        var response = generalAboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getData())
            .isEqualTo(expectedResponseData), // if expected properties are removed, it must return expectedResponseData
            () -> verify(finremCaseDetailsMapper, never()).mapToFinremCaseData(TESTING_DATA_IN_MAP),
            () -> {
                // To ensure method handle(FinremCallbackRequest, String) invoked and CallbackRequest was converted to FinremCallbackRequest
                ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
                verify(generalAboutToSubmitCallbackHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
                assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
            }
        );
    }

    @Test
    void givenGeneralFinremCallbackHandler_whenHandled_thenShouldNotRemoveTemporaryFieldsAfterHandledOrNot() {
        CaseDetails originalCaseDetails = CaseDetails.builder().data(new HashMap<>(TESTING_DATA_IN_MAP)).build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(STOP_REPRESENTING_CLIENT.getCcdType())
            .caseDetails(originalCaseDetails)
            .build();
        FinremCaseDetails finremCaseDetailsWithExpectedFinremCaseData = mock(FinremCaseDetails.class);
        FinremCaseData expectedFinremCaseData = mock(FinremCaseData.class);
        when(finremCaseDetailsWithExpectedFinremCaseData.getData()).thenReturn(expectedFinremCaseData);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(
            argThat(arg -> TESTING_DATA_IN_MAP.equals(arg.getData())))
        ).thenReturn(finremCaseDetailsWithExpectedFinremCaseData);

        var response = generalFinremCallbackHandler.handle(callbackRequest, AUTH_TOKEN);
        assertAll(
            () -> assertThat(response.getData())
                .isEqualTo(expectedFinremCaseData),
            () -> verify(finremCaseDetailsMapper, never()).mapToFinremCaseData(Map.of("retained", YesOrNo.YES)),
            () -> {
                // To ensure method handle(FinremCallbackRequest, String) invoked and CallbackRequest was converted to FinremCallbackRequest
                ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
                verify(generalFinremCallbackHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
                assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
            }
        );
    }

    @Test
    void givenGeneralSubmittedHandler_whenHandled_thenShouldPopulateConfirmationHeaderAndConfirmationBody() {
        assertThat(generalSubmittedCallbackHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN))
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody)
            .containsExactly("# TEST", "<ul><li><h2>Message One</h2></li><li><h2>Message Two</h2></li></ul>");
    }

    @Test
    void givenHandlerWithResponseWithoutWarnings_whenHandled_thenShouldPopulateError() {
        var response = responseWithoutWarningsTestHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN);
        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getErrors,
                GenericAboutToStartOrSubmitCallbackResponse::getWarnings
            ).containsExactly(
                List.of("ERROR12345"), List.of()
            )
        ;
    }

    @Test
    void givenHandlerWithResponse_whenHandled_thenShouldPopulateError() {
        var response = responseTestHandler.handle(FinremCallbackRequestFactory.from(), AUTH_TOKEN);
        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getErrors,
                GenericAboutToStartOrSubmitCallbackResponse::getWarnings,
                GenericAboutToStartOrSubmitCallbackResponse::getState
            ).containsExactly(
                List.of("ERROR_1111"), List.of("WARNING_1111"), "POST_STATE"
            )
        ;
    }

    @Test
    void givenHandler_whenValidateCaseData_thenThrowException() {
        assertAll(
            () ->
                assertThrows(InvalidCaseDataException.class, () ->
                    validateCaseDataTestHandler.handle((FinremCallbackRequest) null, AUTH_TOKEN)),
            () ->
                assertThrows(InvalidCaseDataException.class, () ->
                    validateCaseDataTestHandler.handle(FinremCallbackRequestFactory.from((FinremCaseDetails) null), AUTH_TOKEN)),
            () ->
                assertThrows(InvalidCaseDataException.class, () ->
                    validateCaseDataTestHandler.handle(FinremCallbackRequestFactory.from(FinremCaseDetails.builder().build()), AUTH_TOKEN)),
            () ->
                assertDoesNotThrow(() ->
                    validateCaseDataTestHandler.handle(FinremCallbackRequestFactory.from(
                        FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build()), AUTH_TOKEN))
        );
    }
}
