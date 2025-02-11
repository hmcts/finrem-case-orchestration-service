package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremContestedSendOrderCorresponder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedSubmittedHandlerTest {

    private static final String UUID = java.util.UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();

    @InjectMocks
    private SendOrderContestedSubmittedHandler sendOrderContestedSubmittedHandler;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private CcdService ccdService;
    @Mock
    private FinremContestedSendOrderCorresponder contestedSendOrderCorresponder;

    @Test
    void testCanHandle() {
        assertCanHandle(sendOrderContestedSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER);
    }

    private void setupData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
    }

    private void setupGeneralOrderData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        OrderToShare selected1 = OrderToShare.builder().documentId("app_docs.pdf").documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
    }

    @Test
    void givenPrepareForHearingPostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        caseDetails.getData().getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.PREPARE_FOR_HEARING.getCcdType());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenClosePostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        caseDetails.getData().getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.CLOSE);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.CLOSE.getCcdType());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenOrderSentPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        caseDetails.getData().getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.SEND_ORDER.getCcdType());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenNoPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.NONE);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenContestedCase_whenSolicitorsAreDigitalButOnlySelectedPartyIsApplicant_thenSendContestOrderApprovedEmail() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(List.of(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(List.of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(ccdService);
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenContestedCase_whenNoOrderToSend_thenDoNotAnySortOfCorrespondence() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.NONE);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(ccdService);
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenRespIsDigital_WhenPartSelectedToShareOrderIsRespondent_ThenSendContestOrderApprovedEmailToRespondent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(List.of(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.RESP_SOLICITOR.getCcdCode()));
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verifyNoInteractions(ccdService);
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenAppSolIsDigital_WhenPartSelectedToShareOrderIsApplicabt_ThenSendContestOrderApprovedEmailToApplicant() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(singletonList(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.APP_SOLICITOR.getCcdCode()));

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenAppSolIsNotDigital_WhenPartSelectedToShareOrderIsApplicabt_ThenSendContestOrderApprovedLetter() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupGeneralOrderData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(singletonList(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.getSendOrderWrapper().setAdditionalDocument(caseDocument());

        data.setDirectionDetailsCollection(singletonList(DirectionDetailCollection.builder()
            .value(DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build()).build()));
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));

        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenAppSolIsDigital_WhenApplicantSelectedToHearingShareOrder_ThenSendContestOrderApprovedEmailToApplicant() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(singletonList(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.setUploadHearingOrder(of(DirectionOrderCollection.builder()
            .id(UUID)
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.APP_SOLICITOR.getCcdCode()));
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }


    @Test
    void givenAppSolIsNotDigital_WhenApplicantSelectedToHearingShareOrder_ThenSendContestOrderApprovedViaBulkPrint() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        FinremCaseData data = caseDetails.getData();
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());
        data.setFinalOrderCollection(singletonList(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(new CaseDocument()).build()).build()));
        data.getSendOrderWrapper().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.setUploadHearingOrder(of(DirectionOrderCollection.builder()
            .id(UUID)
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .solicitorEmail("app@sol.com").respondentSolicitorEmail("res@sol.com").build());

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.RESP_SOLICITOR.getCcdCode()));
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list =  new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_SOLICITOR_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_SOLICITOR_4.getCcdCode());
    }

    private DynamicMultiSelectListElement getElementList(String role) {
        return DynamicMultiSelectListElement.builder()
            .code(role)
            .label(role)
            .build();
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

}