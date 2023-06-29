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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ContestedSendOrderCorresponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEND_ORDER_POST_STATE_OPTION_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendOrderContestedSubmittedHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    public static final String PREPARE_FOR_HEARING_STATE = "prepareForHearing";
    public static final String CLOSE_STATE = "close";
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ContestedSendOrderCorresponder contestedSendOrderCorresponder;

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
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            PostStateOption.PREPARE_FOR_HEARING.getCcdField());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails, EventType.PREPARE_FOR_HEARING.getCcdType());
    }

    @Test
    public void givenClosePostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            PostStateOption.CLOSE.getCcdField());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails, EventType.CLOSE.getCcdType());
    }

    @Test
    public void givenOrderSentPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD,
            PostStateOption.ORDER_SENT.getCcdField());

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any());
    }

    @Test
    public void givenNoPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        callbackRequest.getCaseDetails().getData().put(SEND_ORDER_POST_STATE_OPTION_FIELD, null);

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any());
    }

    @Test
    public void givenAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmail() {

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(contestedSendOrderCorresponder).sendCorrespondence(any(CaseDetails.class));
    }

    @Test
    public void givenNotAgreedToReceiveEmails_WhenHandle_ThenDoNotSendContestOrderApprovedEmail() {

        sendOrderContestedSubmittedHandler.handle(buildCallbackRequest(), AUTH_TOKEN);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(contestedSendOrderCorresponder);
    }

    @Test
    public void givenRespAgreedToReceiveEmails_WhenHandle_ThenSendContestOrderApprovedEmailToRespondent() {

        sendOrderContestedSubmittedHandler.handle(createCallbackRequestWithFinalOrder(), AUTH_TOKEN);

        verify(contestedSendOrderCorresponder).sendCorrespondence(any(CaseDetails.class));
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        caseDetails.setData(caseData);
        return CallbackRequest.builder().eventId(EventType.SEND_ORDER.getCcdType())
            .caseDetails(caseDetails).build();
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