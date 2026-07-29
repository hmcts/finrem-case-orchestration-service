package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedAboutToStartHandlerTest {

    @InjectMocks
    private SolicitorCreateContestedAboutToStartHandler underTest;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_START, CONTESTED, SOLICITOR_CREATE);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData()).isEqualTo(callbackRequest.getFinremCaseData());
        verify(onStartDefaultValueService).defaultCivilPartnershipField(callbackRequest);
        verify(onStartDefaultValueService).defaultTypeOfApplication(callbackRequest);
        verify(onStartDefaultValueService).defaultUrgencyQuestion(callbackRequest);
        verify(onStartDefaultValueService).setEstimatedAssetsChecklistVersion(callbackRequest);
    }
}
