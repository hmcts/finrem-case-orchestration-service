package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderApplicantDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerFourDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerOneDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerThreeDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerTwoDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderRespondentDocumentHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToSubmitHandlerTest {

    private static final String UUID_1 = UUID.fromString("d607c045-878e-475f-ab8e-b2f667d8af64").toString();
    private static final String UUID_2 = UUID.fromString("22222222-878e-475f-ab8e-b2f667d8af64").toString();

    private SendOrderContestedAboutToSubmitHandler handler;
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
    @Mock
    private SendOrdersCategoriser sendOrdersCategoriser;

    @BeforeEach
    public void setUpTest() {
        handler = new SendOrderContestedAboutToSubmitHandler(finremCaseDetailsMapper,
            generalOrderService,
            genericDocumentService,
            documentHelper,
            List.of(
                new SendOrderApplicantDocumentHandler(consentOrderApprovedDocumentService, notificationService,
                    caseDataService),
                new SendOrderRespondentDocumentHandler(consentOrderApprovedDocumentService, notificationService,
                    caseDataService),
                new SendOrderIntervenerOneDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerTwoDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerThreeDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerFourDocumentHandler(consentOrderApprovedDocumentService, notificationService)),
            dateService, sendOrdersCategoriser);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER);
    }

    @Test
    void givenContestedCase_whenNoOrderAvailable_thenHandlerDoNothing() {

        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();

        assertNull(caseData.getPartiesOnCase());

        verify(generalOrderService).getParties(callbackRequest.getCaseDetails());
        verify(generalOrderService).hearingOrdersToShare(callbackRequest.getCaseDetails(), List.of());

        verify(genericDocumentService, never()).stampDocument(any(), any(), any(), any());
        verify(documentHelper, never()).getStampType(caseData);
    }

    @Test
    void givenContestedCase_whenAnyOfMethodFails_thenHandlerThrowError() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        when(generalOrderService.hearingOrdersToShare(eq(caseDetails), anyList())).thenThrow(RuntimeException.class);

        Exception exception = Assert.assertThrows(RuntimeException.class,
            () -> handler.handle(callbackRequest, AUTH_TOKEN));
        assertNull(exception.getMessage());
    }

    @Test
    void givenContestedCase_whenOrderAvailableButNoParty_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(new DynamicMultiSelectList());

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID_1).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrderToShare selected2 = OrderToShare.builder().documentId(UUID_2).documentName("app_docs2.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build(),
                OrderToShareCollection.builder().value(selected2).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
        data.getSendOrderWrapper().setAdditionalDocument(caseDocument());
        data.setOrderApprovedCoverLetter(caseDocument());
        List<CaseDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(caseDocument());

        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());

        when(generalOrderService.getParties(caseDetails)).thenReturn(new ArrayList<>());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1, selected2))).thenReturn(Pair.of(caseDocuments, List.of()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any(String.class)))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertNull(caseData.getPartiesOnCase().getValue());
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertNull(caseData.getOrderWrapper().getRespOrderCollection());
        assertNull(caseData.getSendOrderWrapper().getAdditionalDocument());
        verify(genericDocumentService).stampDocument(any(), any(), any(), any());
        verify(documentHelper).getStampType(caseData);
    }

    @Test
    void givenContestedCase_whenOrderAvailableButNoParty_thenHandlerHandleRequest_edgeCaseWhereGeneralOrderAvailableButNotDoc() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(new DynamicMultiSelectList());

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID_1).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrderToShare selected2 = OrderToShare.builder().documentId(UUID_2).documentName("app_docs2.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build(),
                OrderToShareCollection.builder().value(selected2).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
        data.getSendOrderWrapper().setAdditionalDocument(caseDocument());
        data.setOrderApprovedCoverLetter(caseDocument());
        List<CaseDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(caseDocument());

        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollectionWithoutDoc());


        when(generalOrderService.getParties(caseDetails)).thenReturn(new ArrayList<>());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1, selected2))).thenReturn(Pair.of(caseDocuments, List.of()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), any(String.class)))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertNull(caseData.getPartiesOnCase().getValue());
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertNull(caseData.getOrderWrapper().getRespOrderCollection());
        assertNull(caseData.getSendOrderWrapper().getAdditionalDocument());
        verify(genericDocumentService).stampDocument(any(), any(), any(), any());
        verify(documentHelper).getStampType(caseData);
        verify(generalOrderService, never()).isSelectedOrderMatches(eq(List.of(selected1, selected2)), any(ContestedGeneralOrder.class));
    }

    @Test
    @SuppressWarnings("java:S5961")
    void givenContestedCase_whenOrderAvailableToShareWithParties_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID_1).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrderToShare selected2 = OrderToShare.builder().documentId("app_docs.pdf").documentName("app_docs2.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build(),
                OrderToShareCollection.builder().value(selected2).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
        data.getSendOrderWrapper().setAdditionalDocument(caseDocument());
        data.setOrderApprovedCoverLetter(caseDocument());
        List<CaseDocument> caseDocuments = new ArrayList<>();
        caseDocuments.add(caseDocument());
        data.getGeneralOrderWrapper().setGeneralOrders(getGeneralOrderCollection());

        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1, selected2))).thenReturn(Pair.of(caseDocuments, List.of()));
        // below mocking is used for the second invocation on the event where the selectedDocs is cleared in the 1st about-to-submit logic.
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of())).thenReturn(Pair.of(List.of(), List.of()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(CaseDocument.class), eq(AUTH_TOKEN), anyString())).thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size(), "selected parties on case");
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertNull(caseData.getOrderWrapper().getAppOrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getAppOrderCollections().size());
        assertNull(caseData.getOrderWrapper().getRespOrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getRespOrderCollections().size());
        assertEquals(3, caseData.getOrdersSentToPartiesCollection().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);
        verify(dateService).addCreatedDateInFinalOrder(any(), any());

        response = handler.handle(callbackRequest, AUTH_TOKEN);

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

        verify(genericDocumentService, times(1)).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService, times(2)).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper, times(1)).getStampType(caseData);
        verify(dateService, times(1)).addCreatedDateInFinalOrder(any(), any());
    }

    @Test
    void givenContestedCase_whenOrderAvailableToStampAndNoAdditionalDocumentUploaded_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());
        List<DirectionOrderCollection> orderList = new ArrayList<>();
        DirectionOrderCollection order = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument("http://abc/docurl", "abc.pdf", "http://abc/binaryurl"))
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build()).build();
        orderList.add(order);
        data.setUploadHearingOrder(orderList);
        data.setOrderApprovedCoverLetter(caseDocument("http://abc/coversheet", "coversheet.pdf"));

        OrderToShare selected1 = OrderToShare.builder().documentId("abc").documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(any(), any())).thenReturn(false);
        when(dateService.addCreatedDateInFinalOrder(any(), any())).thenReturn(orderList);
        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1))).thenReturn(Pair.of(List.of(caseDocument()), List.of()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(2, caseData.getFinalOrderCollection().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);
    }

    @Test
    void givenContestedCase_whenAlreadyStampedOrderThen_handleAndDoNotStampAgain() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setOrderApprovedCoverLetter(caseDocument("coversheet", "coversheet"));
        data.setPartiesOnCase(getParties());
        List<DirectionOrderCollection> orderList = new ArrayList<>();
        CaseDocument caseDocument = caseDocument("docurl", "abc.pdf", "binaryurl");
        DirectionOrderCollection order = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build()).build();
        orderList.add(order);
        data.setUploadHearingOrder(orderList);
        data.setFinalOrderCollection(orderList);

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID_1).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(any(), any())).thenReturn(false);
        when(dateService.addCreatedDateInFinalOrder(any(), any())).thenReturn(orderList);
        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1))).thenReturn(Pair.of(List.of(caseDocument), List.of()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(1, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertEquals(1, caseData.getFinalOrderCollection().size());

        verify(genericDocumentService, never()).stampDocument(any(), any(), any(), anyString());
        verify(documentHelper, never()).getStampType(caseData);
    }

    @Test
    void givenContestedCase_whenAdditionalHearingDocumentAlreadyDisplayed_thenDoesNotAddAdditionalHearingDocumentToNewColl() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        String additionalHearingDocumentUrl = "http://dm-store:8080/documents/123456-654321-123456-654321";

        String additionalHearingDocumentFilename = "AdditionalHearingDocument.pdf";

        data.setPartiesOnCase(getParties());
        ApproveOrder additionalHearingOrder = ApproveOrder.builder().orderReceivedAt(
                LocalDateTime.of(LocalDate.of(2019, 12, 31),
                    LocalTime.of(6, 30, 45)))
            .caseDocument(caseDocument(additionalHearingDocumentUrl,
                additionalHearingDocumentFilename,
                additionalHearingDocumentUrl + "/binary"))
            .build();
        ApproveOrder previousOrder = ApproveOrder.builder().orderReceivedAt(LocalDateTime.now())
            .build();
        ApproveOrdersHolder approveOrdersHolder = ApproveOrdersHolder.builder().orderReceivedAt(LocalDateTime.now())
            .approveOrders(of(ApprovedOrderCollection.builder().value(additionalHearingOrder).build())
            ).build();
        ApproveOrdersHolder previousOrdersHolder = ApproveOrdersHolder.builder().orderReceivedAt(LocalDateTime.now().minusDays(2))
            .approveOrders(of(ApprovedOrderCollection.builder().value(previousOrder).build())
            ).build();
        ApprovedOrderConsolidateCollection existingCollection1 = ApprovedOrderConsolidateCollection.builder().value(approveOrdersHolder).build();
        ApprovedOrderConsolidateCollection existingCollection2 = ApprovedOrderConsolidateCollection.builder().value(previousOrdersHolder).build();
        List<ApprovedOrderConsolidateCollection> mutableList = new ArrayList<>();
        mutableList.add(existingCollection1);
        mutableList.add(existingCollection2);
        data.getOrderWrapper().setIntv1OrderCollections(mutableList);
        String coverLetterDocumentFilename = "contestedOrderApprovedCoverLetter.pdf";
        String coverLetterUrl = "http://dm-store:8080/documents/129456-654321-123456-654321";
        data.setOrderApprovedCoverLetter(caseDocument(coverLetterUrl, coverLetterDocumentFilename, "http://dm-store:8080/documents/129456-654321-123456-654321/binary"));

        OrderToShare selected1 = OrderToShare.builder().documentId(UUID_1).documentName("app_docs.pdf").documentToShare(YesOrNo.YES).build();
        OrdersToSend ordersToSend = OrdersToSend.builder()
            .value(of(
                OrderToShareCollection.builder().value(selected1).build()
            ))
            .build();

        data.getSendOrderWrapper().setOrdersToSend(ordersToSend);

        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, List.of(selected1))).thenReturn(Pair.of(List.of(caseDocument()), List.of()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(documentHelper.hasAnotherHearing(any(FinremCaseData.class))).thenReturn(true);
        when(documentHelper.getLatestAdditionalHearingDocument(any(FinremCaseData.class)))
            .thenReturn(Optional.of(Optional.of(caseDocument(additionalHearingDocumentUrl,
                    additionalHearingDocumentFilename,
                    additionalHearingDocumentUrl + "/binary"))
                .orElse(null)));
        when(genericDocumentService.stampDocument(
            any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getOrderWrapper().getIntv1OrderCollection());
        assertEquals(2, caseData.getOrderWrapper().getIntv1OrderCollections().size());
        assertEquals(2, caseData.getOrderWrapper().getIntv1OrderCollections().get(0).getValue().getApproveOrders().size());
        assertEquals("app_docs.pdf", caseData.getOrderWrapper().getIntv1OrderCollections().get(0).getValue().getApproveOrders().get(0)
            .getValue().getCaseDocument().getDocumentFilename());
        assertEquals("contestedOrderApprovedCoverLetter.pdf",
            caseData.getOrderWrapper().getIntv1OrderCollections().get(0).getValue().getApproveOrders().get(1)
            .getValue().getCaseDocument().getDocumentFilename());
        assertEquals("AdditionalHearingDocument.pdf",
            caseData.getOrderWrapper().getIntv1OrderCollections().get(1).getValue().getApproveOrders().get(0)
            .getValue().getCaseDocument().getDocumentFilename());
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

    private List<ContestedGeneralOrderCollection> getGeneralOrderCollectionWithoutDoc() {
        ContestedGeneralOrder generalOrder = ContestedGeneralOrder
            .builder()
            .dateOfOrder(LocalDate.of(2002, 2, 5))
            .judge("Moj")
            .generalOrderText("general order")
            .build();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(generalOrder).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        return collections;
    }

    private List<ContestedGeneralOrderCollection> getGeneralOrderCollection() {
        ContestedGeneralOrder generalOrder = ContestedGeneralOrder
            .builder()
            .dateOfOrder(LocalDate.of(2002, 2, 5))
            .judge("Moj")
            .generalOrderText("general order")
            .additionalDocument(caseDocument())
            .build();

        ContestedGeneralOrderCollection collection = ContestedGeneralOrderCollection.builder().value(generalOrder).build();
        List<ContestedGeneralOrderCollection> collections = new ArrayList<>();
        collections.add(collection);
        return collections;
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

    @Test
    void givenContestedCase_whenCoversheetIsMissing_thenShowAnErrorMessage() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());
        List<DirectionOrderCollection> orderList = new ArrayList<>();
        DirectionOrderCollection order = DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument("http://abc/docurl", "abc.pdf", "http://abc/binaryurl"))
            .orderDateTime(LocalDateTime.now()).isOrderStamped(YesOrNo.YES).build()).build();
        orderList.add(order);
        data.setUploadHearingOrder(orderList);
        data.getSendOrderWrapper().setOrdersToSend(OrdersToSend.builder().build());
        data.setOrderApprovedCoverLetter(null);

        when(generalOrderService.hearingOrdersToShare(any(FinremCaseDetails.class), anyList()))
            .thenReturn(Pair.of(List.of(caseDocument()), List.of()));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors())
            .containsExactly("orderApprovedCoverLetter is missing unexpectedly");
    }

    @Test
    void givenContestedCase_whenSendingADraftOrderDocReviewOrder_thenFinalisedOrderIsGenerated() {
        CaseDocument caseDocument1 = caseDocument("http://dm-store:8080/documents/d607c045-aaaa-475f-ab8e-b2f667d8af64", "aaa.pdf");

        FinremCaseData.FinremCaseDataBuilder finremCaseDataBuilder = FinremCaseData.builder();
        finremCaseDataBuilder.orderApprovedCoverLetter(caseDocument());
        finremCaseDataBuilder.sendOrderWrapper(SendOrderWrapper.builder().ordersToSend(OrdersToSend.builder().build()).build());
        finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
            .agreedDraftOrderCollection(new ArrayList<>(of(
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder().draftOrder(caseDocument1).build())
                    .build()
            )))
            .draftOrdersReviewCollection(new ArrayList<>(of(
                DraftOrdersReviewCollection.builder()
                    .value(DraftOrdersReview.builder()
                        .draftOrderDocReviewCollection(new ArrayList<>(of(
                            DraftOrderDocReviewCollection.builder()
                                .value(DraftOrderDocumentReview.builder()
                                    .draftOrderDocument(caseDocument1)
                                    .submittedBy("SUBMITTED BY AAA")
                                    .submittedDate(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                                    .approvalDate(LocalDateTime.of(2024, 12, 31, 2, 59, 59))
                                    .approvalJudge("Mr Judge A")
                                    .build())
                                .build()))
                        ).build())
                    .build()
            )))
            .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseDataBuilder.build());

        when(generalOrderService.hearingOrdersToShare(any(FinremCaseDetails.class), anyList()))
            .thenReturn(Pair.of(List.of(), List.of(caseDocument1)));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .isEmpty();
        assertThat(response.getData().getDraftOrdersWrapper().getDraftOrdersReviewCollection())
            .extracting(DraftOrdersReviewCollection::getValue)
            .containsExactly(DraftOrdersReview.builder().draftOrderDocReviewCollection(List.of()).build());
        assertThat(response.getData().getDraftOrdersWrapper().getFinalisedOrdersCollection())
            .containsExactly(FinalisedOrderCollection.builder().value(FinalisedOrder.builder()
                    .finalisedDocument(caseDocument1)
                    .submittedBy("SUBMITTED BY AAA")
                    .submittedDate(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                    .approvalDate(LocalDateTime.of(2024, 12, 31, 2, 59, 59))
                    .approvalJudge("Mr Judge A")
                .build()).build());
    }

    @Test
    void givenContestedCase_whenSendingPsaDocumentReviewOrder_thenFinalisedOrderIsGenerated() {
        CaseDocument caseDocument1 = caseDocument("http://dm-store:8080/documents/d607c045-aaaa-475f-ab8e-b2f667d8af64", "aaa.pdf");
        CaseDocument caseDocument2 = caseDocument("http://dm-store:8080/documents/d607c045-bbbb-475f-ab8e-b2f667d8af64", "bbb.pdf");

        FinremCaseData.FinremCaseDataBuilder finremCaseDataBuilder = FinremCaseData.builder();
        finremCaseDataBuilder.orderApprovedCoverLetter(caseDocument());
        finremCaseDataBuilder.sendOrderWrapper(SendOrderWrapper.builder().ordersToSend(OrdersToSend.builder().build()).build());
        finremCaseDataBuilder.draftOrdersWrapper(DraftOrdersWrapper.builder()
            .agreedDraftOrderCollection(new ArrayList<>(of(
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder().pensionSharingAnnex(caseDocument1).build())
                    .build()
            )))
            .draftOrdersReviewCollection(new ArrayList<>(of(
                DraftOrdersReviewCollection.builder()
                    .value(DraftOrdersReview.builder()
                        .psaDocReviewCollection(new ArrayList<>(of(
                            PsaDocReviewCollection.builder()
                                .value(PsaDocumentReview.builder()
                                    .psaDocument(caseDocument1)
                                    .submittedBy("SUBMITTED BY BBB")
                                    .submittedDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59))
                                    .approvalDate(LocalDateTime.of(2022, 12, 31, 2, 59, 59))
                                    .approvalJudge("Mr Judge B")
                                    .build())
                                .build()
                        )))
                        .draftOrderDocReviewCollection(new ArrayList<>(of(
                            DraftOrderDocReviewCollection.builder()
                                .value(DraftOrderDocumentReview.builder()
                                    .draftOrderDocument(caseDocument2)
                                    .submittedBy("SUBMITTED BY AAA")
                                    .submittedDate(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                                    .approvalDate(LocalDateTime.of(2024, 12, 31, 2, 59, 59))
                                    .approvalJudge("Mr Judge A")
                                    .build())
                                .build()))
                        ).build())
                    .build()
            )))
            .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseDataBuilder.build());

        when(generalOrderService.hearingOrdersToShare(any(FinremCaseDetails.class), anyList()))
            .thenReturn(Pair.of(List.of(), List.of(caseDocument1)));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .isEmpty();
        assertThat(response.getData().getDraftOrdersWrapper().getDraftOrdersReviewCollection())
            .extracting(DraftOrdersReviewCollection::getValue)
            .containsExactly(DraftOrdersReview.builder()
                .psaDocReviewCollection(List.of())
                .draftOrderDocReviewCollection(List.of(
                    DraftOrderDocReviewCollection.builder()
                        .value(DraftOrderDocumentReview.builder()
                            .draftOrderDocument(caseDocument2)
                            .submittedBy("SUBMITTED BY AAA")
                            .submittedDate(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                            .approvalDate(LocalDateTime.of(2024, 12, 31, 2, 59, 59))
                            .approvalJudge("Mr Judge A")
                            .build())
                        .build()
                )).build());
        assertThat(response.getData().getDraftOrdersWrapper().getFinalisedOrdersCollection())
            .containsExactly(FinalisedOrderCollection.builder().value(FinalisedOrder.builder()
                .finalisedDocument(caseDocument1)
                .submittedBy("SUBMITTED BY BBB")
                .submittedDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59))
                .approvalDate(LocalDateTime.of(2022, 12, 31, 2, 59, 59))
                .approvalJudge("Mr Judge B")
                .build()).build());
    }
}
