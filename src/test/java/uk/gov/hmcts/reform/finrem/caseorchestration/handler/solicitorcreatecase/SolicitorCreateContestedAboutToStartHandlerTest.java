package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedAboutToStartHandlerTest {

    private SolicitorCreateContestedAboutToStartHandler underTest;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @BeforeEach
    void setup() {
        underTest =  new SolicitorCreateContestedAboutToStartHandler(finremCaseDetailsMapper,
            onStartDefaultValueService);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest, ABOUT_TO_START, CONTESTED, SOLICITOR_CREATE);
    }

    @Test
    void handle() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();

        underTest.handle(finremCallbackRequest, AUTH_TOKEN);

        verify(onStartDefaultValueService).defaultCivilPartnershipField(finremCallbackRequest);
        verify(onStartDefaultValueService).defaultTypeOfApplication(finremCallbackRequest);
        verify(onStartDefaultValueService).defaultUrgencyQuestion(finremCallbackRequest);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(Long.valueOf(CASE_ID)).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(SOLICITOR_CREATE).caseDetails(caseDetails).build();
    }
}
