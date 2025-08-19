package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedMhSubmittedHandlerTest {

    @Mock
    private FinremCaseDetailsMapper caseDetailsMapper;

    @InjectMocks
    private UploadApprovedOrderContestedMhSubmittedHandler handler;

    private static final String AUTH_TOKEN = "authToken";

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @Test
    void handle_shouldReturnResponseWhenAddHearingNotChosen() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        ManageHearingsWrapper manageHearingsWrapper = mock(ManageHearingsWrapper.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        assertEquals(caseData, response.getData());
    }

    @Test
    void handle_shouldReturnResponseWhenAddHearingChosen() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        ManageHearingsWrapper manageHearingsWrapper = mock(ManageHearingsWrapper.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.YES);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        assertEquals(caseData, response.getData());
    }
}
