package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.mockRunWithRetryWithHandlerInvokesFirstErrorHandler;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.HWF_ACCEPTED_AND_ISSUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HwfAcceptedAndIssueSubmittedHandlerTest {

    private final String expectedConfirmationHeader = "HWF accepted and issued with errors";

    @InjectMocks
    private HwfAcceptedAndIssueSubmittedHandler handler;

    @Mock
    private RetryExecutor retryExecutor;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, HWF_ACCEPTED_AND_ISSUE);
    }

    @BeforeEach
    void setup() {
        lenient().doNothing().when(retryExecutor).runWithRetryWithHandler(any(), anyString(), any(), any());
    }

    @Test
    void givenCase_whenSendHwfCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending HWF correspondence"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem sending HWF correspondence. Please send it manually.")
                .doesNotContain("There was a problem sending issue application correspondence. Please send it manually.")
                .doesNotContain("There was a problem granting access to respondent solicitor")
        );
    }

    @Test
    void givenCase_whenSendHwfCorrespondenceFailedAndIssueApplicationCorrespondenceFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending HWF correspondence"
        );
        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "sending issue application correspondence"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .contains("There was a problem sending HWF correspondence. Please send it manually.")
                .contains("There was a problem sending issue application correspondence. Please send it manually.")
                .doesNotContain("There was a problem granting access to respondent solicitor")
        );
    }

    @Test
    void givenCase_whenGrantRespondentSolicitorErrorFailed_thenPopulateErrorToConfirmationBody() {
        // Arrange
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        mockRunWithRetryWithHandlerInvokesFirstErrorHandler(
            retryExecutor,
            "granting respondent solicitor"
        );

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).contains(expectedConfirmationHeader),
            () -> assertThat(response.getConfirmationBody())
                .doesNotContain("There was a problem sending HWF correspondence. Please send it manually.")
                .doesNotContain("There was a problem sending issue application correspondence. Please send it manually.")
                .contains("There was a problem granting access to respondent solicitor: " + TEST_RESP_SOLICITOR_EMAIL)
        );
    }

    @Test
    void givenNoErrorsEvolved_whenHandled_thenDoesNotPopulateErrorToConfirmationBody() {
        FinremCaseData spiedFinremCaseData = spy(
            FinremCaseData.builder()
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .consentedRespondentRepresented(YesOrNo.YES).build())
                .build()
        );
        when(spiedFinremCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_RESP_SOLICITOR_EMAIL);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(spiedFinremCaseData);

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // then
        assertAll(
            () -> assertThat(response.getConfirmationHeader()).isNull(),
            () -> assertThat(response.getConfirmationBody()).isNull()
        );
    }
}
