package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderApplicantDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerFourDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerOneDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerThreeDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerTwoDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderRespondentDocumentHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToSubmitHandlerTest {

    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();

    private SendOrderContestedAboutToSubmitHandler sendOrderContestedAboutToSubmitHandler;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OrderDateService dateService;

    @BeforeEach
    public void setUpTest() {
        sendOrderContestedAboutToSubmitHandler = new SendOrderContestedAboutToSubmitHandler(finremCaseDetailsMapper, generalOrderService,
            genericDocumentService, documentHelper, List.of(
                new SendOrderApplicantDocumentHandler(consentOrderApprovedDocumentService, notificationService, caseDataService),
                new SendOrderRespondentDocumentHandler(consentOrderApprovedDocumentService, notificationService, caseDataService),
                new SendOrderIntervenerOneDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerTwoDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerThreeDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerFourDocumentHandler(consentOrderApprovedDocumentService, notificationService)),
            dateService);
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventClose_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendOrderContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_ORDER),
            is(false));
    }

    @Test
    void givenContestedCase_whenNoOrderAvailable_thenHandlerDoNothing() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();

        assertNull(caseData.getPartiesOnCase());

        verify(generalOrderService).getParties(callbackRequest.getCaseDetails());
        verify(generalOrderService).hearingOrdersToShare(callbackRequest.getCaseDetails(), null);

        verify(genericDocumentService, never()).stampDocument(any(), any(), any(), any());
        verify(documentHelper, never()).getStampType(caseData);
    }


    @Test
    void givenContestedCase_whenAnyOfMethodFails_thenHandlerThrowError() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(generalOrderService.hearingOrdersToShare(caseDetails, caseDetails.getData().getOrdersToShare()))
            .thenThrow(RuntimeException.class);

        Exception exception = Assert.assertThrows(RuntimeException.class,
            () -> sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN));
        assertNull(exception.getMessage());
    }

    @Test
    void givenContestedCase_whenOrderAvailableButNoParty_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(new DynamicMultiSelectList());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build(), DynamicMultiSelectListElement.builder()
                .code("app_docs.pdf")
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
        data.setAdditionalDocument(caseDocument());
        data.setOrderApprovedCoverLetter(caseDocument());
        List<CaseDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(caseDocument());
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());

        when(generalOrderService.getParties(caseDetails)).thenReturn(new ArrayList<>());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(caseDocuments);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any(String.class)))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertNull(caseData.getPartiesOnCase().getValue());
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertNull(caseData.getOrderWrapper().getRespOrderCollection());
        assertNull(caseData.getAdditionalDocument());
        verify(genericDocumentService).stampDocument(any(), any(), any(), any());
        verify(documentHelper).getStampType(caseData);

    }

    @Test
    @SuppressWarnings("java:S5961")
    void givenContestedCase_whenOrderAvailableToShareWithParties_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build(), DynamicMultiSelectListElement.builder()
                .code("app_docs.pdf")
                .label("app_docs.pdf")
                .build()))
            .listItems(List.of(DynamicMultiSelectListElement.builder()
                .code(uuid)
                .label("app_docs.pdf")
                .build()))
            .build();

        data.setOrdersToShare(selectedDocs);
        data.setAdditionalDocument(caseDocument());
        data.setOrderApprovedCoverLetter(caseDocument());
        List<CaseDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(caseDocument());
        data.getGeneralOrderWrapper().setGeneralOrderLatestDocument(caseDocument());

        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(caseDocuments);
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());
        when(generalOrderService.isSelectedOrderMatches(selectedDocs, caseDocument())).thenReturn(true);
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size(), "selected parties on case");
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getAppOrderCollections().size());
        assertNull(caseData.getOrderWrapper().getRespOrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getRespOrderCollections().size());
        assertEquals(4, caseData.getOrdersSentToPartiesCollection().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);
        verify(dateService).addCreatedDateInFinalOrder(any(), any());

        response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size(), "selected parties on case");
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(2, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        List<ApprovedOrderConsolidateCollection> intv1OrderCollections = caseData.getOrderWrapper().getIntv1OrderCollections();
        LocalDateTime orderReceivedAt1 = intv1OrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAt2 = intv1OrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAt1.isAfter(orderReceivedAt2));

        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertEquals(2, caseData.getOrderWrapper().getAppOrderCollections().size());
        List<ApprovedOrderConsolidateCollection> appOrderCollections = caseData.getOrderWrapper().getAppOrderCollections();
        LocalDateTime orderReceivedAt1a = appOrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAt2a = appOrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAt1a.isAfter(orderReceivedAt2a));

        assertNull(caseData.getOrderWrapper().getRespOrderCollection());

        assertEquals(2, caseData.getOrderWrapper().getRespOrderCollections().size());
        List<ApprovedOrderConsolidateCollection> respOrderCollections = caseData.getOrderWrapper().getRespOrderCollections();
        LocalDateTime orderReceivedAtR1 = respOrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAtR2 = respOrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAtR1.isAfter(orderReceivedAtR2));

        verify(genericDocumentService, times(2)).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService, times(2)).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper, times(2)).getStampType(caseData);
        verify(dateService, times(2)).addCreatedDateInFinalOrder(any(), any());
    }

    @Test
    void givenContestedCase_whenOrderAvailableToStampAndNoAdditionalDocumentUploaded_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());
        List<DirectionOrderCollection> orderList = new ArrayList<>();
        DirectionOrderCollection order = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument("docurl","abc.pdf", "binaryurl"))
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build()).build();
        orderList.add(order);
        data.setUploadHearingOrder(orderList);

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
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(any(), any())).thenReturn(false);
        when(dateService.addCreatedDateInFinalOrder(any(), any())).thenReturn(orderList);
        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(of(caseDocument()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertEquals(2, caseData.getFinalOrderCollection().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);

    }

    @Test
    void givenContestedCase_whenAlreadyStampedOrderThen_handleAndDoNotStampAgain() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());
        List<DirectionOrderCollection> orderList = new ArrayList<>();
        CaseDocument caseDocument = caseDocument("docurl", "abc.pdf", "binaryurl");
        DirectionOrderCollection order = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build()).build();
        orderList.add(order);
        data.setUploadHearingOrder(orderList);
        data.setFinalOrderCollection(orderList);

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
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(any(), any())).thenReturn(false);
        when(dateService.addCreatedDateInFinalOrder(any(), any())).thenReturn(orderList);
        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(of(caseDocument));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertEquals(1, caseData.getFinalOrderCollection().size());

        verify(genericDocumentService, never()).stampDocument(any(), any(), any(), anyString());
        verify(documentHelper, never()).getStampType(caseData);

    }

    private DynamicMultiSelectList getParties() {

        List<DynamicMultiSelectListElement> list = new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));
        return DynamicMultiSelectList.builder()
            .value(list)
            .listItems(list)
            .build();
    }

    private List<String> partyList() {
        return of(CaseRole.APP_SOLICITOR.getCcdCode(), CaseRole.APP_BARRISTER.getCcdCode(),
            CaseRole.RESP_SOLICITOR.getCcdCode(), CaseRole.RESP_BARRISTER.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_1.getCcdCode(), CaseRole.INTVR_BARRISTER_1.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_2.getCcdCode(), CaseRole.INTVR_BARRISTER_2.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_3.getCcdCode(), CaseRole.INTVR_BARRISTER_3.getCcdCode(),
            CaseRole.INTVR_SOLICITOR_4.getCcdCode(), CaseRole.INTVR_BARRISTER_4.getCcdCode());
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
