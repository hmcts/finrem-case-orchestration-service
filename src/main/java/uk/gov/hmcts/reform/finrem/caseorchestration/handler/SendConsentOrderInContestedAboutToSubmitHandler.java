package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderPartyDocumentHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SendConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler<FinremCaseDataContested> {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList;


    public SendConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           GeneralOrderService generalOrderService,
                                                           GenericDocumentService genericDocumentService,
                                                           ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                           ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService,
                                                           List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
        this.sendOrderPartyDocumentList = sendOrderPartyDocumentList;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_CONSENT_IN_CONTESTED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(
        FinremCallbackRequest<FinremCaseDataContested> callbackRequest, String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {}, callback {} for case id {}",
            callbackRequest.getEventType(), CallbackType.ABOUT_TO_SUBMIT, caseId);

        try {
            FinremCaseDataContested caseData = caseDetails.getData();
            List<String> parties = generalOrderService.getParties(caseDetails);
            log.info("Selected parties {} on case id {}", parties, caseId);

            List<OrderSentToPartiesCollection> printOrderCollection = new ArrayList<>();

            setUpOrderDocumentsOnCase(caseDetails,
                                      printOrderCollection,
                                      userAuthorisation,
                                      parties);

            caseData.setOrdersSentToPartiesCollection(printOrderCollection);
        } catch (RuntimeException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
            .data(caseDetails.getData()).build();
    }

    private OrderSentToPartiesCollection addToPrintOrderCollection(CaseDocument document) {
        return OrderSentToPartiesCollection.builder()
            .value(SendOrderDocuments.builder().caseDocument(document).build())
            .build();
    }

    private void setUpOrderDocumentsOnCase(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                           List<OrderSentToPartiesCollection> printOrderCollection,
                                           String userAuthorisation,
                                           List<String> parties) {
        String caseId = caseDetails.getId().toString();
        FinremCaseDataContested caseData = caseDetails.getData();
        log.info("Setting up order documents for case {}:", caseId);
        List<CaseDocument> consentOrderDocumentPack;
        ConsentOrderWrapper wrapper = caseData.getConsentOrderWrapper();
        List<DocumentCollection> additionalDocuments = getAdditionalDocuments(caseData, userAuthorisation, caseId, printOrderCollection);

        if (consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, userAuthorisation)) {
            List<ConsentOrderCollection> approvedConsentOrders = caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
            ConsentOrderCollection latestApprovedConsentOrder = approvedConsentOrders.get(approvedConsentOrders.size() - 1);
            consentOrderDocumentPack = createApprovedOrderDocumentPack(latestApprovedConsentOrder);
            sendOrderPartyDocumentList.forEach(
                handler -> handler.setUpConsentOrderApprovedDocumentsOnCase(caseDetails, parties, approvedConsentOrders, additionalDocuments));
        } else {
            CaseDocument latestGeneralOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();
            CaseDocument latestRefusedConsentOrder = null;
            if (wrapper.getConsentedNotApprovedOrders() != null && !wrapper.getConsentedNotApprovedOrders().isEmpty()) {
                latestRefusedConsentOrder = wrapper.getConsentedNotApprovedOrders()
                    .get(wrapper.getConsentedNotApprovedOrders().size() - 1).getApprovedOrder().getConsentOrder();
            }

            CaseDocument latestOrderDocument = consentOrderNotApprovedDocumentService.getLatestOrderDocument(
                    latestRefusedConsentOrder, latestGeneralOrder, userAuthorisation);
            consentOrderDocumentPack = List.of(latestOrderDocument);
            sendOrderPartyDocumentList.forEach(
                handler -> handler.setUpConsentOrderUnapprovedDocumentsOnCase(caseDetails, parties, latestOrderDocument, additionalDocuments));
        }
        sendOrderPartyDocumentList.forEach(
            handler -> handler.setUpCoverSheetOnCase(caseDetails, parties, userAuthorisation));

        consentOrderDocumentPack.forEach(doc -> genericDocumentService.convertDocumentIfNotPdfAlready(doc, userAuthorisation, caseId));
        consentOrderDocumentPack.forEach(doc -> printOrderCollection.add(addToPrintOrderCollection(doc)));
    }

    private List<CaseDocument> createApprovedOrderDocumentPack(ConsentOrderCollection latestApprovedConsentOrder) {
        List<CaseDocument> approvedConsentOrderDocumentPack = new ArrayList<>();
        CaseDocument consentOrder = latestApprovedConsentOrder.getApprovedOrder().getConsentOrder();
        CaseDocument orderLetter = latestApprovedConsentOrder.getApprovedOrder().getOrderLetter();
        List<CaseDocument> pensionCaseDocuments = new ArrayList<>();
        List<PensionTypeCollection> pensionDocuments = latestApprovedConsentOrder.getApprovedOrder().getPensionDocuments();
        if (consentOrder != null) {
            approvedConsentOrderDocumentPack.add(consentOrder);
        }
        if (orderLetter != null) {
            approvedConsentOrderDocumentPack.add(orderLetter);
        }
        if (pensionDocuments != null && !pensionDocuments.isEmpty()) {
            pensionDocuments.forEach(doc -> pensionCaseDocuments.add(doc.getTypedCaseDocument().getPensionDocument()));
            approvedConsentOrderDocumentPack.addAll(pensionCaseDocuments);
        }

        return approvedConsentOrderDocumentPack;
    }

    private List<DocumentCollection> getAdditionalDocuments(FinremCaseDataContested caseData,
                                                            String userAuthorisation,
                                                            String caseId,
                                                            List<OrderSentToPartiesCollection> printOrderCollection) {

        List<CaseDocument> documents = new ArrayList<>();
        List<DocumentCollection> caseDocuments = new ArrayList<>();
        if (caseData.getAdditionalCicDocuments() != null) {
            caseData.getAdditionalCicDocuments().forEach(doc -> documents.add(doc.getValue()));
        }

        if (!documents.isEmpty()) {
            log.info("Additional uploaded documents {} to be sent with consent or general order for case id {}", documents, caseId);
            List<CaseDocument> pdfDocuments = documents.stream().map(doc ->
                    genericDocumentService.convertDocumentIfNotPdfAlready(doc, userAuthorisation, caseId)).toList();
            pdfDocuments.forEach(doc -> {
                printOrderCollection.add(addToPrintOrderCollection(doc));
                caseDocuments.add(DocumentCollection.builder().value(doc).build());
            });
        }
        return caseDocuments;

    }
}
