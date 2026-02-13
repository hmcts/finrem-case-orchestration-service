package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypeOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private IssueApplicationContestedAboutToSubmitHandler handler;
    @Mock
    private OnlineFormDocumentService service;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenIssueApplication_thenGenerateAppropriateCaseDocumentAndSetDefaultIfValueIsMissing() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(123L).data(FinremCaseData.builder().scheduleOneWrapper(ScheduleOneWrapper.builder().build()).build()).build();
        FinremCallbackRequest callbackRequest
            = FinremCallbackRequest.builder().eventType(EventType.ISSUE_APPLICATION).caseDetails(caseDetails).build();

        when(service.generateContestedMiniForm(AUTH_TOKEN, callbackRequest.getCaseDetails())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = response.getData();

        assertEquals("123", data.getDivorceCaseNumber());
        assertEquals(TypeOfApplication.MATRIMONIAL_CIVILPARTNERSHIP.getTypeOfApplication(),
            data.getScheduleOneWrapper().getTypeOfApplication().getValue());
        assertEquals(caseDocument(), data.getMiniFormA());
    }

    @Test
    void givenCase_whenIssueApplication_thenGenerateAppropriateCaseDocumentDoNotSetIfAlreadySetValue() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(123L).data(FinremCaseData.builder().divorceCaseNumber("897901").scheduleOneWrapper(ScheduleOneWrapper.builder()
                    .typeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989)
                .build()).build()).build();
        FinremCallbackRequest callbackRequest
            = FinremCallbackRequest.builder().eventType(EventType.ISSUE_APPLICATION).caseDetails(caseDetails).build();

        when(service.generateContestedMiniForm(AUTH_TOKEN, callbackRequest.getCaseDetails())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData data = response.getData();

        assertEquals("897901", data.getDivorceCaseNumber());
        assertEquals(TypeOfApplication.SCHEDULE_ONE.getTypeOfApplication(),
            data.getScheduleOneWrapper().getTypeOfApplication().getValue());
        assertEquals(caseDocument(), data.getMiniFormA());
    }
}
