package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.SendOrderPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEND_ORDER_POST_STATE_OPTION_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedSubmittedHandlerTest {

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private CcdService ccdService;

    @InjectMocks
    private SendOrderContestedSubmittedHandler sendOrderContestedSubmittedHandler;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    public void givenPrepareForHearingPostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        CallbackRequest callbackRequest = createCallbackRequestWithFinalOrder();
        callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            SendOrderPostStateOption.PREPARE_FOR_HEARING.getCcdType());
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), EventType.PREPARE_FOR_HEARING.getCcdType());
    }

    @Test
    public void givenClosePostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        CallbackRequest callbackRequest = createCallbackRequestWithFinalOrder();
        callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            SendOrderPostStateOption.PREPARE_FOR_HEARING.getCcdType());
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), EventType.CLOSE.getCcdType());
    }

    @Test
    public void givenOrderSentPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        CallbackRequest callbackRequest = createCallbackRequestWithFinalOrder();
        callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            SendOrderPostStateOption.PREPARE_FOR_HEARING.getCcdType());
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any());
    }

    @Test
    public void givenAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmail() {
        when(caseDataService.isApplicantSolicitorAgreeToReceiveEmails(any())).thenReturn(true);

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(notificationService).sendContestOrderApprovedEmailApplicant(any());
    }

    @Test
    public void givenNotAgreedToReceiveEmails_WhenHandle_ThenDoNotSendContestOrderApprovedEmail() {

        sendOrderContestedSubmittedHandler.handle(buildCallbackRequest(), AUTH_TOKEN);

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenRespAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmailToRespondent() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(true);

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(notificationService).sendContestOrderApprovedEmailRespondent(any());
    }

    @Test
    public void givenToggleOff_WhenHandle_ThenDoNotSendEmail() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any());
    }

    @Test
    public void givenRespNotAgreedToReceiveEmails_WhenHandle_ThenDoNotSendContestOrderApprovedEmailToRespondent() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(notificationService.shouldEmailRespondentSolicitor(any())).thenReturn(false);

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }

    private CallbackRequest createCallbackRequestWithFinalOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        ArrayList<HearingOrderCollectionData> finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument
                .builder()
                .uploadDraftDocument(new CaseDocument())
                .build())
            .build());

        callbackRequest.getCaseDetails().getData().put(FINAL_ORDER_COLLECTION, finalOrderCollection);

        return callbackRequest;
    }
}