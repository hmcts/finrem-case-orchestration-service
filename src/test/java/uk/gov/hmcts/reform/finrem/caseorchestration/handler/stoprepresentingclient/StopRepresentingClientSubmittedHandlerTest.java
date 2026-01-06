package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient.StopRepresentingClientService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private StopRepresentingClientService stopRepresentingClientService;
    @Mock
    private CaseRoleService caseRoleService;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientSubmittedHandler(finremCaseDetailsMapper, caseRoleService, stopRepresentingClientService);
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenAnyCase_whenHandled_thenPublishStopRepresentingClientEvent(boolean invokedByIntervener) {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            caseDataBefore, caseData);

        when(caseRoleService.isIntervenerRepresentative(caseData, AUTH_TOKEN)).thenReturn(invokedByIntervener);

        underTest.handle(request, AUTH_TOKEN);

        ArgumentCaptor<StopRepresentingClientInfo> eventCaptor = ArgumentCaptor.forClass(StopRepresentingClientInfo.class);
        verify(stopRepresentingClientService, timeout(500)).applyCaseAssignment(eventCaptor.capture());

        assertThat(eventCaptor.getValue().getCaseDetails().getData()).isEqualTo(caseData);
        assertThat(eventCaptor.getValue().getCaseDetailsBefore().getData()).isEqualTo(caseDataBefore);
        assertThat(eventCaptor.getValue().getUserAuthorisation()).isEqualTo(AUTH_TOKEN);
        assertThat(eventCaptor.getValue().isInvokedByIntervener()).isEqualTo(invokedByIntervener);
    }
}
