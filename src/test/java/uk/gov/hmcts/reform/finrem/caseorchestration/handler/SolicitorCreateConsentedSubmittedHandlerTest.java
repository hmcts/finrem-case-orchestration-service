package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateConsentedSubmittedHandlerTest {

    @InjectMocks
    private SolicitorCreateConsentedSubmittedHandler handler;

    @Mock
    private CreateCaseService createCaseService;
    @Mock
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(any(), any());
    }

    @Test
    public void givenACcdCallBackSolicitorCreateContestedCase_WhenHandle_thenSetSolicitorRole() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(assignApplicantSolicitorService, times(1)).setApplicantSolicitor(callbackRequest, AUTH_TOKEN);
    }

    @Test
    public void givenACcdCallBackSolicitorCreateContestedCase_WhenHandle_WhenAssignAppSolServiceThrows_ReturnErrorList() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();
        String expectedMsg = "Failed to assign applicant solicitor to case, please ensure you have selected the correct applicant organisation on case";

        doThrow(new AssignCaseAccessException(expectedMsg)).when(assignApplicantSolicitorService)
            .setApplicantSolicitor(callbackRequest, AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response =  handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(response.getErrors().get(0), expectedMsg);
    }

    private CaseDetails getCase() {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}