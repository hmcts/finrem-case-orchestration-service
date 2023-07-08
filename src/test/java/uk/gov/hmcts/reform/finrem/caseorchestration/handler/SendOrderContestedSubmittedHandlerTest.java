package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremContestedSendOrderCorresponder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedSubmittedHandlerTest {
    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();
    private static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private SendOrderContestedSubmittedHandler sendOrderContestedSubmittedHandler;

    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private CcdService ccdService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private FinremContestedSendOrderCorresponder contestedSendOrderCorresponder;


    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    private void setupData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
    }

    private void setupGeneralOrderData(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code("app_docs.pdf")
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code("app_docs.pdf")
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
    }

    @Test
    void givenPrepareForHearingPostStateOption_WhenHandle_ThenRunPrepareForHearingEvent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        setupData(caseDetails);
        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.CLOSE);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService, never()).executeCcdEventOnCase(AUTH_TOKEN, caseDetails.getId().toString(),
            caseDetails.getCaseType().getCcdType(), EventType.SEND_ORDER.getCcdType());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    @Test
    void givenNoPostStateOption_WhenHandle_ThenDoNotRunUpdateCaseAndStateIsOrderSent() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData().setSendOrderPostStateOption(SendOrderEventPostStateOption.NONE);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(List.of(CaseRole.APP_SOLICITOR.getValue(), CaseRole.RESP_SOLICITOR.getValue()));
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.NONE);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.ORDER_SENT);

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.RESP_SOLICITOR.getValue()));
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.APP_SOLICITOR.getValue()));

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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.setAdditionalDocument(caseDocument());

        data.setDirectionDetailsCollection(singletonList(DirectionDetailCollection.builder()
            .value(DirectionDetail.builder().isAnotherHearingYN(YesOrNo.YES).build()).build()));
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getValue(), CaseRole.RESP_SOLICITOR.getValue()));

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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.setUploadHearingOrder(of(DirectionOrderCollection.builder()
            .id(uuid)
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(singletonList(CaseRole.APP_SOLICITOR.getValue()));
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
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
        data.setSendOrderPostStateOption(SendOrderEventPostStateOption.PREPARE_FOR_HEARING);
        data.setUploadHearingOrder(of(DirectionOrderCollection.builder()
            .id(uuid)
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument())
                .build())
            .build()));
        data.setContactDetailsWrapper(ContactDetailsWrapper.builder()
            .solicitorEmail("app@sol.com").respondentSolicitorEmail("res@sol.com").build());

        when(generalOrderService.getParties(any(FinremCaseDetails.class)))
            .thenReturn(of(CaseRole.APP_SOLICITOR.getValue(), CaseRole.RESP_SOLICITOR.getValue()));
        when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
        sendOrderContestedSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(ccdService).executeCcdEventOnCase(any(), any(), any(), any());
        verify(contestedSendOrderCorresponder).sendCorrespondence(any(), any());
    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list =  new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));

        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getValue())
                .label(CaseRole.APP_SOLICITOR.getValue())
                .build()))
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getValue(),
            CaseRole.RESP_SOLICITOR.getValue(), CaseRole.INTVR_SOLICITOR_1.getValue(), CaseRole.INTVR_SOLICITOR_2.getValue(),
            CaseRole.INTVR_SOLICITOR_3.getValue(), CaseRole.INTVR_SOLICITOR_4.getValue());
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