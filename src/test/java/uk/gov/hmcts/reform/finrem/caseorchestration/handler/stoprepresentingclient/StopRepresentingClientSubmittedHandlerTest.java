package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientSubmittedHandlerTest {

    private StopRepresentingClientSubmittedHandler underTest;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientSubmittedHandler(finremCaseDetailsMapper, applicationEventPublisher);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(SUBMITTED, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(SUBMITTED, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenAnyCase_whenHandled_thenReturnConfirmationMessages() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(request, AUTH_TOKEN);
        assertThat(response.getConfirmationBody()).isEqualTo("<br /><br />");
        assertThat(response.getConfirmationHeader()).isEqualTo("# Notice of change request submitted");
    }

    @Test
    void givenAnyCase_whenHandled_thenPublishStopRepresentingClientEvent() {
        FinremCaseData caseData = FinremCaseData.builder()
            .build();

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            CONTESTED, caseData);

        underTest.handle(request, AUTH_TOKEN);

        ArgumentCaptor<StopRepresentingClientEvent>  eventCaptor = ArgumentCaptor.forClass(StopRepresentingClientEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue().getCaseId()).isEqualTo(CASE_ID);
        assertThat(eventCaptor.getValue().getUserAuthorisation()).isEqualTo(AUTH_TOKEN);
    }
}
