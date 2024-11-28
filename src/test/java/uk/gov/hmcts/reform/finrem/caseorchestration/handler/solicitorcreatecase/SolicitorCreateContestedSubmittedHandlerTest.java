package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignApplicantSolicitorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedSubmittedHandlerTest {

    private SolicitorCreateContestedSubmittedHandler handler;

    @Mock
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CreateCaseService createCaseService;

    @BeforeEach
    public void setup() {
        handler =  new SolicitorCreateContestedSubmittedHandler(finremCaseDetailsMapper, assignApplicantSolicitorService,
            createCaseService);
    }

    @Test
    void givenACcdCallbackSolicitorCreateContestedCase_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    void givenACcdCallbackSolicitorCreateContestedCase_WhenHandleWithAppRepresentation_thenAddSupplementaryAndAppRep() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(YesOrNo.YES);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(callbackRequest, AUTH_TOKEN);
        verify(assignApplicantSolicitorService, times(1))
            .setApplicantSolicitor(callbackRequest.getCaseDetails(), AUTH_TOKEN);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void givenACcdCallbackSolicitorCreateContestedCase_WhenHandleWithAppRepresentationError_thenAddSupplementaryAndHandleException() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(YesOrNo.YES);

        doThrow(new AssignCaseAccessException("Exception message"))
            .when(assignApplicantSolicitorService)
            .setApplicantSolicitor(any(), any());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(callbackRequest, AUTH_TOKEN);
        verify(assignApplicantSolicitorService, times(1))
            .setApplicantSolicitor(callbackRequest.getCaseDetails(), AUTH_TOKEN);

        assertTrue(response.getErrors().contains("Exception message"));
        assertTrue(response.getErrors()
            .contains("Failed to assign applicant solicitor to case, "
                + "please ensure you have selected the correct applicant organisation on case"));
    }

    @Test
    void givenACcdCallbackSolicitorCreateContestedCase_WhenHandleWithNoAppRepresentation_thenApplicantSolicitorNotAssigned() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(YesOrNo.NO);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        verify(createCaseService, times(1)).setSupplementaryData(callbackRequest, AUTH_TOKEN);
        verify(assignApplicantSolicitorService, never()).setApplicantSolicitor(any(), any());
        assertTrue(response.getErrors().isEmpty());
    }

    private FinremCallbackRequest buildFinremCallbackRequest(YesOrNo isApplicantRepresented) {
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper.builder().applicantRepresented(isApplicantRepresented).build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.SOLICITOR_CREATE).caseDetails(caseDetails).build();
    }
}