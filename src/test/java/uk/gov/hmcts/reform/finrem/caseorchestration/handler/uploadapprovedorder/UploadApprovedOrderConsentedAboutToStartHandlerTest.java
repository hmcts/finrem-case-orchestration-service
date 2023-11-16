package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderConsentedAboutToStartHandlerTest {

    @Mock
    private IdamService idamService;
    @InjectMocks
    private UploadApprovedOrderConsentedAboutToStartHandler uploadApprovedOrderConsentedAboutToStartHandler;

    @Test
    public void givenConsentedCase_whenAboutToStartUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderConsentedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenConsentedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderConsentedAboutToStartHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenUploadConsentedApproveOrder_whenHandle_thenAddDefaultJudgeAndCurrentDate() {

        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build();
        when(idamService.getIdamSurname("auth")).thenReturn("judge");

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            uploadApprovedOrderConsentedAboutToStartHandler.handle(callbackRequest, "auth");
        FinremCaseData caseData = response.getData();
        assertThat(caseData.getOrderDirectionJudgeName(), is("judge"));
        assertThat(caseData.getOrderDirectionDate(), is(LocalDate.now()));

        assertNull(caseData.getOrderDirectionAbsolute());
        assertNull(caseData.getServePensionProvider());
        assertNull(caseData.getServePensionProviderResponsibility());
        assertNull(caseData.getServePensionProviderOther());
        assertNull(caseData.getOrderDirectionJudge());
        assertNull(caseData.getOrderDirectionAddComments());
        assertNull(caseData.getConsentOrderWrapper().getUploadApprovedConsentOrder());
    }
}