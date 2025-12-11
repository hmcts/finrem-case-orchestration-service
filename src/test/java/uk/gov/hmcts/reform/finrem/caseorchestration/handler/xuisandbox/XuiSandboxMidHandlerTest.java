package uk.gov.hmcts.reform.finrem.caseorchestration.handler.xuisandbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class XuiSandboxMidHandlerTest {

    @InjectMocks
    private XuiSandboxMidHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX_ONE),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.XUI_SANDBOX_TWO)
        );
    }

    @Test
    void shouldHandle() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(OrganisationPolicy.builder().build());
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(OrganisationPolicy.builder().build());
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        when(callbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenApplicantOrganisationPolicyIsNull() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(null);
        lenient().when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(OrganisationPolicy.builder().build());
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        when(callbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        assertThatThrownBy(() -> handler.handle(callbackRequest, AUTH_TOKEN)).hasMessage("ApplicantOrganisationPolicy is null");
    }

    @Test
    void shouldThrowExceptionWhenRespondentOrganisationPolicyIsNull() {
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getCaseType()).thenReturn(CaseType.CONSENTED);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getApplicantOrganisationPolicy()).thenReturn(OrganisationPolicy.builder().build());
        when(finremCaseData.getRespondentOrganisationPolicy()).thenReturn(null);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
        when(callbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        assertThatThrownBy(() -> handler.handle(callbackRequest, AUTH_TOKEN)).hasMessage("RespondentOrganisationPolicy is null");
    }
}
