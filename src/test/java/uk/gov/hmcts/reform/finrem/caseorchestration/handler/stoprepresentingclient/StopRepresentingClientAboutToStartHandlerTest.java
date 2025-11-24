package uk.gov.hmcts.reform.finrem.caseorchestration.handler.stoprepresentingclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientAboutToStartHandlerTest {

    @Mock
    private CaseRoleService caseRoleService;

    @InjectMocks
    private StopRepresentingClientAboutToStartHandler underTest;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(ABOUT_TO_START, CONTESTED, STOP_REPRESENTING_CLIENT),
            Arguments.of(ABOUT_TO_START, CONSENTED, STOP_REPRESENTING_CLIENT));
    }

    @Test
    void givenAsApplicantSolicitor_whenHandled_thenPopulateCorrectLabel() {
        when(caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(APP_SOLICITOR);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            FinremCaseData.builder().build());
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Applicant's contact details private from the Respondent?");

        verify(caseRoleService).getUserCaseRole(CASE_ID, AUTH_TOKEN);
    }

    @Test
    void givenAsRespondentSolicitor_whenHandled_thenPopulateCorrectLabel() {
        when(caseRoleService.getUserCaseRole(CASE_ID, AUTH_TOKEN)).thenReturn(RESP_SOLICITOR);

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(Long.valueOf(CASE_ID),
            FinremCaseData.builder().build());
        FinremCaseData finremCaseData = underTest.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(finremCaseData.getStopRepresentationWrapper().getClientAddressForServiceConfidentialLabel())
            .isEqualTo("Keep the Respondent's contact details private from the Applicant?");

        verify(caseRoleService).getUserCaseRole(CASE_ID, AUTH_TOKEN);
    }
}
