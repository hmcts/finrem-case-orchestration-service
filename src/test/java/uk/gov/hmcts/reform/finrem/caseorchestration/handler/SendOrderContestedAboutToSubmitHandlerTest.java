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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
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

    @BeforeEach
    public void setUpTest() {
        sendOrderContestedAboutToSubmitHandler = new SendOrderContestedAboutToSubmitHandler(finremCaseDetailsMapper, generalOrderService,
            genericDocumentService, documentHelper, List.of(
                new SendOrderApplicantDocumentHandler(),
                new SendOrderRespondentDocumentHandler(),
                new SendOrderIntervenerOneDocumentHandler(),
                new SendOrderIntervenerTwoDocumentHandler(),
                new SendOrderIntervenerThreeDocumentHandler(),
                new SendOrderIntervenerFourDocumentHandler())
        );
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
        assertNull(caseData.getIntv1OrderCollection());
        assertNull(caseData.getAppOrderCollection());
        assertNull(caseData.getRespOrderCollection());
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
        assertNull(caseData.getIntv1OrderCollection());
        assertEquals(1, caseData.getIntv1OrderCollections().size());
        assertNull(caseData.getAppOrderCollection());
        assertEquals(1, caseData.getAppOrderCollections().size());
        assertNull(caseData.getRespOrderCollection());
        assertEquals(1, caseData.getRespOrderCollections().size());
        assertEquals(4, caseData.getOrdersSentToPartiesCollection().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);


        response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size(), "selected parties on case");
        assertEquals(2, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getIntv1OrderCollection());
        assertEquals(2, caseData.getIntv1OrderCollections().size());
        List<ApprovedOrderConsolidateCollection> intv1OrderCollections = caseData.getIntv1OrderCollections();
        LocalDateTime orderReceivedAt1 = intv1OrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAt2 = intv1OrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAt1.isAfter(orderReceivedAt2));

        assertNull(caseData.getAppOrderCollection());
        assertEquals(2, caseData.getAppOrderCollections().size());
        List<ApprovedOrderConsolidateCollection> appOrderCollections = caseData.getAppOrderCollections();
        LocalDateTime orderReceivedAt1a = appOrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAt2a = appOrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAt1a.isAfter(orderReceivedAt2a));

        assertNull(caseData.getRespOrderCollection());

        assertEquals(2, caseData.getRespOrderCollections().size());
        List<ApprovedOrderConsolidateCollection> respOrderCollections = caseData.getRespOrderCollections();
        LocalDateTime orderReceivedAtR1 = respOrderCollections.get(0).getValue().getOrderReceivedAt();
        LocalDateTime orderReceivedAtR2 = respOrderCollections.get(1).getValue().getOrderReceivedAt();

        assertTrue(orderReceivedAtR1.isAfter(orderReceivedAtR2));

        verify(genericDocumentService, times(2)).stampDocument(any(), any(), any(), anyString());
        verify(generalOrderService, times(2)).isSelectedOrderMatches(any(), any());
        verify(genericDocumentService).convertDocumentIfNotPdfAlready(any(), any(), anyString());
        verify(documentHelper, times(2)).getStampType(caseData);

    }

    @Test
    void givenContestedCase_whenOrderAvailableToStampAndNoAdditionalDocumentUploaded_thenHandlerHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
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

        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(of(caseDocument()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(12, caseData.getPartiesOnCase().getValue().size());
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getIntv1OrderCollection());
        assertEquals(1, caseData.getIntv1OrderCollections().size());

        verify(genericDocumentService).stampDocument(any(), any(), any(), anyString());
        verify(documentHelper).getStampType(caseData);

    }

    @Test
    void givenContestedCase_whenAdditionalHearingDocumentAlreadyDisplayed_thenDoesNotAddAdditionalHearingDocumentToNewColl() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        String additionalHearingDocumentUrl = "http://dm-store:8080/documents/123456-654321-123456-654321";
        String coverLetterUrl = "http://dm-store:8080/documents/129456-654321-123456-654321";
        String additionalHearingDocumentFilename = "AdditionalHearingDocument.pdf";
        String coverLetterDocumentFilename = "contestedOrderApprovedCoverLetter.pdf";
        String previousOrderDocumentUrl = "http://dm-store:8080/documents/929756-654321-123456-654381";
        String previousOrderDocumentFilename = "PreviousOrder.pdf";
        data.setPartiesOnCase(getParties());
        ApproveOrder additionalHearingOrder = ApproveOrder.builder().orderReceivedAt(LocalDateTime.of(LocalDate.of(2019, 12, 31), LocalTime.of(6, 30, 45)))
            .caseDocument(caseDocument(additionalHearingDocumentUrl, additionalHearingDocumentFilename, additionalHearingDocumentUrl + "/binary")).build();
        ApproveOrder previousOrder = ApproveOrder.builder().orderReceivedAt(LocalDateTime.now())
            .caseDocument(caseDocument(previousOrderDocumentUrl, previousOrderDocumentFilename, previousOrderDocumentUrl + "/binary")).build();
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
        data.setIntv1OrderCollections(mutableList);
        data.setOrderApprovedCoverLetter(caseDocument(coverLetterUrl, coverLetterDocumentFilename, "http://dm-store:8080/documents/129456-654321-123456-654321/binary"));

        DynamicMultiSelectList selectedDocs = DynamicMultiSelectList.builder().value(List.of(DynamicMultiSelectListElement.builder()
        .code(uuid).label("app_docs.pdf").build())).listItems(List.of(DynamicMultiSelectListElement.builder()
        .code(uuid).label("app_docs.pdf").build())).build();

        data.setOrdersToShare(selectedDocs);

        when(generalOrderService.getParties(caseDetails)).thenReturn(partyList());
        when(generalOrderService.hearingOrdersToShare(caseDetails, selectedDocs)).thenReturn(of(caseDocument()));
        when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(StampType.FAMILY_COURT_STAMP);
        when(documentHelper.hasAnotherHearing(any(FinremCaseData.class))).thenReturn(true);
        when(documentHelper.getLatestAdditionalHearingDocument(any(FinremCaseData.class))).thenReturn(Optional.of(Optional.of(caseDocument(additionalHearingDocumentUrl, additionalHearingDocumentFilename, additionalHearingDocumentUrl + "/binary")).orElse(null)));
        when(genericDocumentService.stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN), eq(StampType.FAMILY_COURT_STAMP), anyString()))
            .thenReturn(caseDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = sendOrderContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertEquals(1, caseData.getFinalOrderCollection().size());
        assertNull(caseData.getIntv1OrderCollection());
        assertEquals(3, caseData.getIntv1OrderCollections().size());
        assertThat(caseData.getIntv1OrderCollections().get(0).getValue().getApproveOrders().size(), is(2));
        assertThat(caseData.getIntv1OrderCollections().get(0).getValue().getApproveOrders().get(0)
                .getValue().getCaseDocument().getDocumentFilename(), is("app_docs.pdf"));
        assertThat(caseData.getIntv1OrderCollections().get(0).getValue().getApproveOrders().get(1)
                .getValue().getCaseDocument().getDocumentFilename(), is("contestedOrderApprovedCoverLetter.pdf"));
        assertThat(caseData.getIntv1OrderCollections().get(1).getValue().getApproveOrders().get(0)
                .getValue().getCaseDocument().getDocumentFilename(), is("AdditionalHearingDocument.pdf"));
        assertThat(caseData.getIntv1OrderCollections().get(2).getValue().getApproveOrders().get(0)
                .getValue().getCaseDocument().getDocumentFilename(), is("PreviousOrder.pdf"));
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
