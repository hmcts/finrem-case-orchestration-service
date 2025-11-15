package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;

@ExtendWith(MockitoExtension.class)
class FinremCallbackHandlerTest {

    static class DefaultFinremCallbackHandler extends FinremCallbackHandler {

        public DefaultFinremCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(callbackRequest.getCaseDetails().getData())
                .build();
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    static class DefaultAboutToSubmitCallbackHandler extends FinremAboutToSubmitCallbackHandler {

        public DefaultAboutToSubmitCallbackHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
            super(finremCaseDetailsMapper);
        }

        @Override
        public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
            FinremCallbackRequest callbackRequest, String userAuthorisation) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(callbackRequest.getCaseDetails().getData())
                .build();
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }
    }

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private DefaultFinremCallbackHandler defaultFinremCallbackHandler;
    private DefaultAboutToSubmitCallbackHandler defaultAboutToSubmitCallbackHandler;

    private static  final Map<String, Object> TESTING_DATA_IN_MAP = Map.of(
        "retained", YesOrNo.YES,
        "loginAsApplicantSolicitor", YesOrNo.YES,
        "loginAsRespondentSolicitor", YesOrNo.NO
    );

    @BeforeEach
    void setUp() {
        defaultFinremCallbackHandler = spy(new DefaultFinremCallbackHandler(finremCaseDetailsMapper));
        defaultAboutToSubmitCallbackHandler = spy(new DefaultAboutToSubmitCallbackHandler(finremCaseDetailsMapper));
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

        assertThat(defaultAboutToSubmitCallbackHandler.handle(callbackRequest, AUTH_TOKEN).getData())
            .isEqualTo(expectedResponseData); // if expected properties are removed, it must return expectedResponseData
        verify(finremCaseDetailsMapper, never()).mapToFinremCaseData(TESTING_DATA_IN_MAP);

        // to ensure callbackRequest is a copy i.e. callbackRequest.toBuilder() was invoked
        ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
        verify(defaultAboutToSubmitCallbackHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
        assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
    }

    @Test
    void givenDefaultFinremCallbackHandler_whenHandled_thenShouldNotRemoveTemporaryFieldsAfterHandledOrNot() {
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

        assertThat(defaultFinremCallbackHandler.handle(callbackRequest, AUTH_TOKEN).getData())
            .isEqualTo(expectedFinremCaseData);
        verify(finremCaseDetailsMapper, never()).mapToFinremCaseData(Map.of("retained", YesOrNo.YES));

        ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
        verify(defaultFinremCallbackHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
        assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
    }
}
