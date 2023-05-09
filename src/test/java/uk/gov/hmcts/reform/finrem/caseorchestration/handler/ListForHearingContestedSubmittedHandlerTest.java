package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListForHearingContestedSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @InjectMocks
    private ListForHearingContestedSubmittedHandler handler;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenCase_whenSchedulingFirstTime_thenSendInitialCorrespondence() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(caseDetails)).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any());
    }

    @Test
    public void givenCase_whenSchedulingSecondTime_thenSendAdditionalHearingDocuments() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(true);
        when(hearingDocumentService.alreadyHadFirstHearing(caseDetails)).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(any(), any());
        verify(hearingDocumentService, never()).sendInitialHearingCorrespondence(any(), any());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetailsBefore.setData(caseData);
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SEND_ORDER.getCcdType())
            .caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();
    }

}