package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedSubmittedHandlerTest {

    @InjectMocks
    private SolicitorCreateConsentedSubmittedHandler handler;

    @Mock
    private AssignApplicantSolicitorService assignApplicantSolicitorService;
    @Mock
    private CreateCaseService createCaseService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler = new SolicitorCreateConsentedSubmittedHandler(finremCaseDetailsMapper, assignApplicantSolicitorService,
            createCaseService);
    }

    @Test
    public void givenACcdCallbackSolicitorCreateConsentedCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void givenACcdCallbackSolicitorCreateContestedCase_WhenHandle_thenAddSupplementary() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(eq(callbackRequest), any());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseDataConsented caseData = FinremCaseDataConsented.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.SOLICITOR_CREATE).caseDetails(caseDetails).build();
    }

}