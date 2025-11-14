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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    static class EnabledClearTemporaryFieldHandler extends FinremAboutToSubmitCallbackHandler {

        public EnabledClearTemporaryFieldHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
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
    private EnabledClearTemporaryFieldHandler enabledClearTemporaryFieldHandler;

    @BeforeEach
    void setUp() {
        defaultFinremCallbackHandler = spy(new DefaultFinremCallbackHandler(finremCaseDetailsMapper));
        enabledClearTemporaryFieldHandler = spy(new EnabledClearTemporaryFieldHandler(finremCaseDetailsMapper));
    }

    @Test
    void givenEnabledHandler_whenHandled_thenRemoveTemporaryFieldsOrNot() {
        Map<String, Object> mapWithSessionWrapperProperties = new HashMap<>();
        // properties with @TemporaryField annotation
        // The properties below are taken from SessionWrapper.class
        mapWithSessionWrapperProperties.put("loginAsApplicantSolicitor", YesOrNo.YES);
        mapWithSessionWrapperProperties.put("loginAsRespondentSolicitor", YesOrNo.YES);
        // property without @TemporaryField annotation
        mapWithSessionWrapperProperties.put("retained", YesOrNo.YES);

        CaseDetails caseDetails = CaseDetails.builder().data(mapWithSessionWrapperProperties).build();
        FinremCaseDetails expectedFinremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData expectedFinremCaseData = mock(FinremCaseData.class);
        when(expectedFinremCaseDetails.getData()).thenReturn(expectedFinremCaseData);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(STOP_REPRESENTING_CLIENT.getCcdType())
            .caseDetails(mock(CaseDetails.class))
            .caseDetailsBefore(null)
            .build();

        when(finremCaseDetailsMapper.mapToFinremCaseDetails(callbackRequest.getCaseDetails())).thenReturn(finremCaseDetails);
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);

        Map<String, Object> expectedMapWithNonAnnotatedProperty = Map.of("retained", YesOrNo.YES);
        when(finremCaseDetailsMapper
            .mapToFinremCaseDetails(CaseDetails.builder().data(expectedMapWithNonAnnotatedProperty).build()))
            .thenReturn(expectedFinremCaseDetails);

        assertThat(enabledClearTemporaryFieldHandler.handle(callbackRequest, AUTH_TOKEN).getData())
            .isEqualTo(expectedFinremCaseData);

        // to ensure callbackRequest is a copy i.e. callbackRequest.toBuilder() was invoked
        ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
        verify(enabledClearTemporaryFieldHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
        assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
    }

    @Test
    void givenDefaultFinremCallbackHandler_whenHandled_thenShouldNotRemoveTemporaryFieldsOrNot() {
        CaseDetails originalCaseDetails = mock(CaseDetails.class);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(STOP_REPRESENTING_CLIENT.getCcdType())
            .caseDetails(originalCaseDetails)
            .caseDetailsBefore(null)
            .build();
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseData expectedFinremCaseData = mock(FinremCaseData.class);
        when(finremCaseDetails.getData()).thenReturn(expectedFinremCaseData);
        when(finremCaseDetailsMapper.mapToFinremCaseDetails(originalCaseDetails)).thenReturn(finremCaseDetails);

        assertThat(defaultFinremCallbackHandler.handle(callbackRequest, AUTH_TOKEN).getData())
            .isEqualTo(expectedFinremCaseData);
        ArgumentCaptor<FinremCallbackRequest> argumentCaptor = ArgumentCaptor.forClass(FinremCallbackRequest.class);
        verify(defaultFinremCallbackHandler).handle(argumentCaptor.capture(), eq(AUTH_TOKEN));
        assertThat(argumentCaptor.getValue().getEventType()).isEqualTo(STOP_REPRESENTING_CLIENT);
    }
}
