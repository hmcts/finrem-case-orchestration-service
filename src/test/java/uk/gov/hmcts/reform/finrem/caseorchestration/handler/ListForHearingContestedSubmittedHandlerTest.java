package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListForHearingContestedSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    @Mock
    private HearingDocumentService hearingDocumentService;
    @Mock
    private AdditionalHearingDocumentService additionalHearingDocumentService;

    @InjectMocks
    private ListForHearingContestedSubmittedHandler handler;

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenCaseTypeIsConsented_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenEventIsClose_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenCase_whenSchedulingFirstTime_thenSendInitialCorrespondence() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetailsBefore.setData(new HashMap<>());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        when(hearingDocumentService.alreadyHadFirstHearing(caseDetails)).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any());
    }

    @Test
    void givenCase_whenSchedulingSecondTime_thenSendAdditionalHearingDocuments() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = CaseDetails.builder().id(123L).build();
        caseDetailsBefore.setData(new HashMap<>());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        when(hearingDocumentService.alreadyHadFirstHearing(caseDetails)).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(additionalHearingDocumentService).sendAdditionalHearingDocuments(any(), any());
        verify(hearingDocumentService, never()).sendInitialHearingCorrespondence(any(), any());
    }

    @Test
    void givenCase_whenCaseDetailsBeforeDoNotExist_thenSendInitialCorrespondence() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(hearingDocumentService).sendInitialHearingCorrespondence(any(), any());
        verify(additionalHearingDocumentService, never()).sendAdditionalHearingDocuments(any(), any());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SEND_ORDER.getCcdType())
            .caseDetails(caseDetails).build();
    }

}