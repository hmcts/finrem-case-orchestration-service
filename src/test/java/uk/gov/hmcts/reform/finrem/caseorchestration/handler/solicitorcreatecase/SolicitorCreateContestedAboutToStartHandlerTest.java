package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedAboutToStartHandlerTest {

    private SolicitorCreateContestedAboutToStartHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @BeforeEach
    void setup() {
        handler =  new SolicitorCreateContestedAboutToStartHandler(finremCaseDetailsMapper,
            onStartDefaultValueService);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CREATE);
    }

    @Test
    void handle() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();

        handler.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(onStartDefaultValueService).defaultCivilPartnershipField(finremCallbackRequest);
        verify(onStartDefaultValueService).defaultTypeOfApplication(finremCallbackRequest);
        verify(onStartDefaultValueService).defaultUrgencyQuestion(finremCallbackRequest);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }
}
