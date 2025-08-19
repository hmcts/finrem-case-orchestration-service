package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedMhAboutToSubmitHandlerTest {

    @Mock
    private UploadApprovedOrderService uploadApprovedOrderService;
    @Mock
    private ManageHearingActionService manageHearingActionService;

    @InjectMocks
    private UploadApprovedOrderContestedMhAboutToSubmitHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @Test
    void handle_shouldProcessApprovedOrdersAndReturnResponseWithoutErrors() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var caseDetailsBefore = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetails.getData()).thenReturn(caseData);

        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.NO);

        String userAuthorisation = "authToken";

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, userAuthorisation);

        verify(uploadApprovedOrderService).processApprovedOrdersMh(caseDetails, caseDetailsBefore, userAuthorisation);
        assertNotNull(response);
    }

    @Test
    void handle_shouldInvokeManageHearingActionServiceWhenAddHearingChosen() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var caseDetailsBefore = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(caseDetails.getData()).thenReturn(caseData);

        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.YES);

        String userAuthorisation = "authToken";

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, userAuthorisation);

        verify(manageHearingActionService).performAddHearing(caseDetails, userAuthorisation);
        verify(manageHearingActionService).updateTabData(caseData);
        assertNotNull(response);
    }
}
