package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrder;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.SendOrderEventPostStateOption;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

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
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    @Test
    public void givenPrepareForHearingPostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getCaseData().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails, EventType.PREPARE_FOR_HEARING.getCcdType());
    }

    @Test
    public void givenClosePostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getCaseData().setSendOrderPostStateOption(SendOrderEventPostStateOption.CLOSE);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails, EventType.CLOSE.getCcdType());
    }

    @Test
    public void givenOrderSentPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any());
    }

    @Test
    public void givenNoPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setSendOrderPostStateOption(null);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any());
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
        when(notificationService.shouldEmailRespondentSolicitor(isA(FinremCaseData.class))).thenReturn(true);

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
        when(notificationService.shouldEmailRespondentSolicitor(isA(FinremCaseData.class))).thenReturn(false);

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(notificationService, never()).sendContestOrderApprovedEmailRespondent(any());
    }

    private CallbackRequest buildCallbackRequest() {
        FinremCaseData caseData = new FinremCaseData();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseData(caseData).build();
        return CallbackRequest.builder().eventType(EventType.PREPARE_FOR_HEARING).caseDetails(caseDetails).build();
    }

    private CallbackRequest createCallbackRequestWithFinalOrder() {
        CallbackRequest callbackRequest = buildCallbackRequest();

        List<DirectionOrderCollection> finalOrderCollection = new ArrayList<>();
        finalOrderCollection.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(new Document())
                .build())
            .build());

        callbackRequest.getCaseDetails().getCaseData().setFinalOrderCollection(finalOrderCollection);

        return callbackRequest;
    }
}