package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private AssignApplicantSolicitorService assignApplicantSolicitorService;
    @Mock
    private CaseDataService caseDataService;

    private SolicitorCreateContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        handler =  new SolicitorCreateContestedAboutToStartHandler(assignApplicantSolicitorService,
            caseDataService, new OnStartDefaultValueService());
        when(caseDataService.isContestedPaperApplication(any(CaseDetails.class))).thenReturn(false);

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
        CallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(assignApplicantSolicitorService, times(1)).setApplicantSolicitor(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenACcdCallBackSolicitorCreateContestedCase_WhenHandle_WhenAssignAppSolServiceThrows_ReturnErrorList() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        String expectedMsg = "Failed to assign applicant solicitor to case, please ensure you have selected the correct"
            + " applicant organisation on case";

        doThrow(new AssignCaseAccessException(expectedMsg)).when(assignApplicantSolicitorService)
            .setApplicantSolicitor(callbackRequest, AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =  handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(response.getErrors().get(0), expectedMsg);
    }

    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        when(caseDataService.isContestedPaperApplication(callbackRequest.getCaseDetails())).thenReturn(true);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(NO_VALUE, response.getData().get(CIVIL_PARTNERSHIP));
        assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO, response.getData().get(TYPE_OF_APPLICATION));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}