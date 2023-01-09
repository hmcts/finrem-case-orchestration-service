package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseDataService caseDataService;

    private SolicitorCreateContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        handler =  new SolicitorCreateContestedAboutToStartHandler(finremCaseDetailsMapper, assignApplicantSolicitorService,
            caseDataService, new OnStartDefaultValueService());
        when(caseDataService.isContestedFinremCasePaperApplication(any(FinremCaseDetails.class))).thenReturn(false);

    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void givenACcdCallBackSolicitorCreateContestedCase_WhenHandle_thenSetSolicitorRole() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(assignApplicantSolicitorService, times(1)).setApplicantSolicitor(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void givenACcdCallBackSolicitorCreateContestedCase_WhenHandle_WhenAssignAppSolServiceThrows_ReturnErrorList() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        String expectedMsg = "Failed to assign applicant solicitor to case, please ensure you have selected the correct"
            + " applicant organisation on case";

        doThrow(new AssignCaseAccessException(expectedMsg)).when(assignApplicantSolicitorService)
            .setApplicantSolicitor(callbackRequest.getCaseDetails(), AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(response.getErrors().get(0), expectedMsg);
    }

    @Test
    public void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        when(caseDataService.isContestedFinremCasePaperApplication(callbackRequest.getCaseDetails())).thenReturn(true);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(YesOrNo.NO, response.getData().getCivilPartnership());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.SOLICITOR_CREATE).caseDetails(caseDetails).build();
    }
}