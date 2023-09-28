package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentGeneratorService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PdfStampingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderApplicantDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerFourDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerOneDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerThreeDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerTwoDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderRespondentDocumentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.List.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class SendConsentOrderInContestedAboutToSubmitHandlerTest {

    private static final String uuid = UUID.fromString("a23ce12a-81b3-416f-81a7-a5159606f5ae").toString();

    private SendConsentOrderInContestedAboutToSubmitHandler sendConsentOrderInContestedAboutToSubmitHandler;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentConfiguration documentConfiguration;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;
    @Mock
    private BulkPrintDocumentGeneratorService bulkPrintDocumentGeneratorService;
    @Mock
    private CaseDataService caseDataService;
    private DocumentConversionService documentConversionService;
    @Mock
    private PdfStampingService pdfStampingService;
    @Mock
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @BeforeEach
    public void setUpTest() {
        sendConsentOrderInContestedAboutToSubmitHandler = new SendConsentOrderInContestedAboutToSubmitHandler(
            finremCaseDetailsMapper,
            generalOrderService,
            genericDocumentService, consentOrderApprovedDocumentService, consentOrderNotApprovedDocumentService, List.of(
                new SendOrderApplicantDocumentHandler(consentOrderApprovedDocumentService, notificationService, caseDataService),
                new SendOrderRespondentDocumentHandler(consentOrderApprovedDocumentService, notificationService, caseDataService),
                new SendOrderIntervenerOneDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerTwoDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerThreeDocumentHandler(consentOrderApprovedDocumentService, notificationService),
                new SendOrderIntervenerFourDocumentHandler(consentOrderApprovedDocumentService, notificationService))
        );
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanHandle() {
        assertThat(sendConsentOrderInContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_CONSENT_IN_CONTESTED_ORDER),
            is(true));
    }

    @Test
    void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitEventSendOrder_thenHandlerCanNotHandle() {
        assertThat(sendConsentOrderInContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.SEND_CONSENT_IN_CONTESTED_ORDER),
            is(false));
    }

    @Test
    void givenConsentInContestedCase_whenApprovedOrdersButNoRefusedOrderAvailableToShareWithParties_thenHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        data.setPartiesOnCase(getParties());

        CaseDocument additionalDocument = caseDocument("additionalDocumentUrl", "additionalDocumentFileName",
            "additionalDocumentBinaryUrl");
        data.setAdditionalCicDocuments(List.of(DocumentCollection.builder().value(additionalDocument).build()));
        ApprovedOrder firstApprovedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder1Url", "consentOrder1Name",
            "consentOrder1Binary")).orderLetter(caseDocument("orderLetter1Url", "orderLetter1Name",
            "OrderLetter1Binary")).build();
        ApprovedOrder secondApprovedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder2Url", "consentOrder2Name",
            "consentOrder2Binary")).orderLetter(caseDocument("orderLetter2Url", "orderLetter2Name",
            "OrderLetter2Binary")).build();
        ConsentOrderCollection firstConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(firstApprovedOrder).build();
        ConsentOrderCollection secondConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(secondApprovedOrder).build();
        when(generalOrderService.getParties(any())).thenReturn(partyList());
        when(genericDocumentService.convertDocumentIfNotPdfAlready(any(CaseDocument.class), any(), any())).thenReturn(additionalDocument);
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(true);
        ConsentOrderWrapper wrapper = data.getConsentOrderWrapper();
        List<ConsentOrderCollection> approvedOrders = List.of(firstConsentOrderCollection, secondConsentOrderCollection);
        wrapper.setContestedConsentedApprovedOrders(approvedOrders);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = sendConsentOrderInContestedAboutToSubmitHandler.handle(
            callbackRequest, AUTH_TOKEN);
        FinremCaseData resultingData = response.getData();
        List<OrderSentToPartiesCollection> partyOrders = resultingData.getOrdersSentToPartiesCollection();
        assertThat(partyOrders.get(0).getValue().getCaseDocument(), equalTo(additionalDocument));
        assertThat(partyOrders.get(1).getValue().getCaseDocument(), equalTo(secondApprovedOrder.getConsentOrder()));
        assertThat(partyOrders.get(2).getValue().getCaseDocument(), equalTo(secondApprovedOrder.getOrderLetter()));
        assertThat(partyOrders.size(), equalTo(3));
    }

    @Test
    void givenConsentInContestedCase_whenRefusedOrdersButNoApprovedOrderAvailableToShareWithParties_thenHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        ConsentOrderWrapper wrapper = data.getConsentOrderWrapper();
        data.setPartiesOnCase(getParties());

        ApprovedOrder firstRefusedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder1Url", "consentOrder1Name",
            "consentOrder1Binary")).build();
        ApprovedOrder secondRefusedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder2Url", "consentOrder2Name",
            "consentOrder2Binary")).build();
        ConsentOrderCollection firstConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(firstRefusedOrder).build();
        ConsentOrderCollection secondConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(secondRefusedOrder).build();
        List<ConsentOrderCollection> refusedOrders = List.of(firstConsentOrderCollection, secondConsentOrderCollection);
        wrapper.setConsentedNotApprovedOrders(refusedOrders);
        when(generalOrderService.getParties(any())).thenReturn(partyList());
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.getLatestOrderDocument(any(), any(), any()))
            .thenReturn(wrapper.getConsentedNotApprovedOrders().get(0).getApprovedOrder().getConsentOrder());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = sendConsentOrderInContestedAboutToSubmitHandler.handle(
            callbackRequest, AUTH_TOKEN);
        FinremCaseData resultingData = response.getData();
        List<OrderSentToPartiesCollection> partyOrders = resultingData.getOrdersSentToPartiesCollection();
        assertThat(partyOrders.get(0).getValue().getCaseDocument(), equalTo(firstRefusedOrder.getConsentOrder()));
    }

    @Test
    void givenConsentInContestedCase_whenLatestOrderIsGeneralOrderAvailableToShareWithParties_thenHandleRequest() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        GeneralOrderWrapper wrapper = data.getGeneralOrderWrapper();
        data.setPartiesOnCase(getParties());
        CaseDocument latestGeneralOrderDocument = caseDocument("generalOrderUrl", "generalOrderName",
            "generalOrderBinary");

        wrapper.setGeneralOrderLatestDocument(latestGeneralOrderDocument);
        when(generalOrderService.getParties(any())).thenReturn(partyList());
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.getLatestOrderDocument(any(), any(), any())).thenReturn(wrapper.getGeneralOrderLatestDocument());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = sendConsentOrderInContestedAboutToSubmitHandler.handle(
            callbackRequest, AUTH_TOKEN);
        FinremCaseData resultingData = response.getData();
        List<OrderSentToPartiesCollection> partyOrders = resultingData.getOrdersSentToPartiesCollection();
        assertThat(partyOrders.get(0).getValue().getCaseDocument(), equalTo(wrapper.getGeneralOrderLatestDocument()));
    }

    @Test
    void givenConsentInContestedCase_whenRefusedOrdersButNoApprovedOrderAvailableToShareAndNoParties_thenAddUnapprovedOrdersToPartyColl() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData data = caseDetails.getData();
        ConsentOrderWrapper wrapper = data.getConsentOrderWrapper();
        data.setPartiesOnCase(getParties());

        ApprovedOrder firstRefusedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder1Url", "consentOrder1Name",
            "consentOrder1Binary")).build();
        ApprovedOrder secondRefusedOrder = ApprovedOrder.builder().consentOrder(caseDocument("consentOrder2Url", "consentOrder2Name",
            "consentOrder2Binary")).build();
        ConsentOrderCollection firstConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(firstRefusedOrder).build();
        ConsentOrderCollection secondConsentOrderCollection = ConsentOrderCollection.builder().approvedOrder(secondRefusedOrder).build();
        List<ConsentOrderCollection> refusedOrders = List.of(firstConsentOrderCollection, secondConsentOrderCollection);
        wrapper.setConsentedNotApprovedOrders(refusedOrders);
        when(consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(any(), any())).thenReturn(false);
        when(consentOrderNotApprovedDocumentService.getLatestOrderDocument(any(), any(), any()))
            .thenReturn(wrapper.getConsentedNotApprovedOrders().get(0).getApprovedOrder().getConsentOrder());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = sendConsentOrderInContestedAboutToSubmitHandler.handle(
            callbackRequest, AUTH_TOKEN);
        FinremCaseData resultingData = response.getData();
        List<OrderSentToPartiesCollection> partyOrders = resultingData.getOrdersSentToPartiesCollection();
        assertThat(partyOrders.get(0).getValue().getCaseDocument(), equalTo(firstRefusedOrder.getConsentOrder()));
    }

    private DynamicMultiSelectList getParties() {
        List<DynamicMultiSelectListElement> list = new ArrayList<>();
        partyList().forEach(role -> list.add(getElementList(role)));
        return DynamicMultiSelectList.builder()
            .value(of(DynamicMultiSelectListElement.builder()
                .code(CaseRole.APP_SOLICITOR.getCcdCode())
                .label(CaseRole.APP_SOLICITOR.getCcdCode())
                .code(CaseRole.RESP_SOLICITOR.getCcdCode())
                .label(CaseRole.RESP_SOLICITOR.getCcdCode())
                .code(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_1.getCcdCode())
                .code(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_2.getCcdCode())
                .code(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_3.getCcdCode())
                .code(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .label(CaseRole.INTVR_SOLICITOR_4.getCcdCode())
                .build()))
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
