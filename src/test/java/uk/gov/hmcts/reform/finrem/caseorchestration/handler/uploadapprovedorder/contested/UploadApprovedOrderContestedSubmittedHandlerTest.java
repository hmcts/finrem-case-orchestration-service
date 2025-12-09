package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedSubmittedHandlerTest {

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @InjectMocks
    private UploadApprovedOrderContestedSubmittedHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @Test
    void handle_shouldReturnResponseWhenAddHearingNotChosen() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);
        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.NO);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertNotNull(response);
        assertEquals(caseData, response.getData());
    }

    @Test
    void handle_shouldReturnResponseWhenAddHearingChosen() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);
        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.YES);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        verify(manageHearingsCorresponder).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
        assertNotNull(response);
        assertEquals(caseData, response.getData());
    }
}
