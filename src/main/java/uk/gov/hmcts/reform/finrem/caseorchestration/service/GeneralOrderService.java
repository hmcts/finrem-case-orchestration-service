package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderAddressTo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderPreviewDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.WithAttachmentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DocumentIdProvider;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralOrderDocumentCategoriser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_BODY_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    private final GeneralOrderDocumentCategoriser generalOrderDocumentCategoriser;
    private final Function<CaseDocument, GeneralOrderPreviewDocument> createGeneralOrderData = this::applyGeneralOrderData;
    private final UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;
    private final BiFunction<CaseDetails, String, CaseDocument> generateDocument = this::applyGenerateDocument;

    public Map<String, Object> createGeneralOrder(String authorisationToken, CaseDetails caseDetails) {
        log.info("Generating General Order for Case ID: {}", caseDetails.getId());

        return generateDocument
            .andThen(createGeneralOrderData)
            .andThen(data -> previewGeneralOrderData(data, caseDetails))
            .apply(documentHelper.deepCopy(caseDetails, CaseDetails.class), authorisationToken);
    }

    public BulkPrintDocument getLatestGeneralOrderAsBulkPrintDocument(Map<String, Object> caseData, String authorisationToken, String caseId) {
        CaseDocument latestGeneralOrder = documentHelper.getLatestGeneralOrder(caseData);
        if (latestGeneralOrder != null) {
            CaseDocument pdfDocument =
                genericDocumentService.convertDocumentIfNotPdfAlready(latestGeneralOrder, authorisationToken, caseId);
            caseData.put(GENERAL_ORDER_LATEST_DOCUMENT, pdfDocument);
            return BulkPrintDocument.builder().binaryFileUrl(pdfDocument.getDocumentBinaryUrl())
                .fileName(pdfDocument.getDocumentFilename()).build();
        }
        return null;
    }

    private CaseDocument applyGenerateDocument(CaseDetails caseDetails, String authorisationToken) {
        return genericDocumentService.generateDocument(authorisationToken, addExtraFields.apply(caseDetails),
            documentConfiguration.getGeneralOrderTemplate(caseDetails),
            getGeneralOrderFileNameWithDateTimeStamp());
    }

    private String getGeneralOrderFileNameWithDateTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeString = now.format(formatter);
        String str = documentConfiguration.getGeneralOrderFileName();
        return new StringBuilder(str).insert(str.length() - 4, "-" + dateTimeString).toString();
    }

    private GeneralOrderPreviewDocument applyGeneralOrderData(CaseDocument caseDocument) {
        return new GeneralOrderPreviewDocument(caseDocument);
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private CaseDetails applyAddExtraFields(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("DivorceCaseNumber", caseDetails.getData().get(DIVORCE_CASE_NUMBER));
        caseData.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));

        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseData.put("RespondentName", documentHelper.getRespondentFullNameConsented(caseDetails));
            caseData.put("GeneralOrderCourt", "SITTING in private");
            caseData.put("GeneralOrderHeaderOne", "Sitting in the Family Court");
        } else {
            caseData.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));
            caseData.put("GeneralOrderCourtSitting", "SITTING AT the Family Court at the ");
            caseData.put("GeneralOrderCourt", CourtHelper.getSelectedCourt(caseDetails));
            caseData.put("GeneralOrderHeaderOne", "In the Family Court");
            caseData.put("GeneralOrderHeaderTwo", "sitting in the");
            caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        }

        caseData.put("GeneralOrderJudgeDetails",
            StringUtils.joinWith(" ",
                caseDetails.getData().get(GENERAL_ORDER_JUDGE_TYPE),
                caseDetails.getData().get(GENERAL_ORDER_JUDGE_NAME)));

        caseData.put("GeneralOrderRecitals", caseDetails.getData().get(GENERAL_ORDER_RECITALS));
        caseData.put("GeneralOrderDate", caseDetails.getData().get(GENERAL_ORDER_DATE));
        caseData.put("GeneralOrderBodyText", caseDetails.getData().get(GENERAL_ORDER_BODY_TEXT));

        return caseDetails;
    }

    private Map<String, Object> previewGeneralOrderData(GeneralOrderPreviewDocument generalOrderData, CaseDetails caseDetails) {
        caseDetails.getData().put(GENERAL_ORDER_PREVIEW_DOCUMENT, generalOrderData.getGeneralOrder());
        return caseDetails.getData();
    }

    public void addConsentedGeneralOrderToCollection(FinremCaseData caseData) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        generalOrderWrapper.setGeneralOrderLatestDocument(generalOrderWrapper.getGeneralOrderPreviewDocument());

        GeneralOrder generalOrder = GeneralOrder.builder()
            .generalOrderAddressTo(getAddressToFormatted(generalOrderWrapper.getGeneralOrderAddressTo()))
            .generalOrderDocumentUpload(generalOrderWrapper.getGeneralOrderPreviewDocument())
            .build();

        GeneralOrderCollectionItem item = new GeneralOrderCollectionItem();
        item.setId(UUID.randomUUID().toString());
        item.setGeneralOrder(generalOrder);

        List<GeneralOrderCollectionItem> generalOrders =
            ofNullable(generalOrderWrapper.getGeneralOrderCollection())
                .orElse(new ArrayList<>());
        generalOrders.add(item);
        generalOrderWrapper.setGeneralOrderCollection(generalOrders);
    }

    public void addContestedGeneralOrderToCollection(FinremCaseData caseData) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders =
            ofNullable(generalOrderWrapper.getGeneralOrders())
                .orElse(new ArrayList<>());
        generalOrderWrapper.setGeneralOrders(generalOrders);

        updateContestedGeneralOrders(caseData, generalOrders);
    }

    public void addConsentedInContestedGeneralOrderToCollection(FinremCaseData caseData) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders =
            ofNullable(generalOrderWrapper.getGeneralOrdersConsent())
                .orElse(new ArrayList<>());
        generalOrderWrapper.setGeneralOrdersConsent(generalOrders);

        updateContestedGeneralOrders(caseData, generalOrders);
    }

    private void updateContestedGeneralOrders(FinremCaseData caseData,
                                              List<ContestedGeneralOrderCollection> generalOrders) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        generalOrderWrapper.setGeneralOrderLatestDocument(new CaseDocument(generalOrderWrapper.getGeneralOrderPreviewDocument()));
        ContestedGeneralOrderCollection contestedGeneralOrderCollection =
            createContestedGeneralOrderCollection(generalOrderWrapper);
        generalOrders.add(contestedGeneralOrderCollection);

        generalOrderDocumentCategoriser.categorise(caseData);
    }

    private ContestedGeneralOrderCollection createContestedGeneralOrderCollection(
        GeneralOrderWrapper generalOrderWrapper) {
        ContestedGeneralOrder contestedGeneralOrder = ContestedGeneralOrder
            .builder()
            .dateOfOrder(generalOrderWrapper.getGeneralOrderDate())
            .additionalDocument(new CaseDocument(generalOrderWrapper.getGeneralOrderPreviewDocument()))
            .generalOrderAddressTo(getAddressToFormatted(generalOrderWrapper.getGeneralOrderAddressTo()))
            .build();
        return ContestedGeneralOrderCollection.builder()
            .value(contestedGeneralOrder)
            .build();
    }

    private String getAddressToFormatted(GeneralOrderAddressTo addressTo) {
        return addressTo != null ? addressTo.getText() : "";
    }

    public void setOrderList(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        List<OrderToShareCollection> orderToShareCollection = new ArrayList<>();

        List<ContestedGeneralOrderCollection> generalOrders = data.getGeneralOrderWrapper().getGeneralOrders();

        if (generalOrders != null && !generalOrders.isEmpty()) {
            generalOrders.sort(Comparator.nullsLast(this::getCompareTo));
            generalOrders.forEach(generalOrder -> {
                ContestedGeneralOrder order = generalOrder.getValue();
                if (order != null && order.getAdditionalDocument() != null) {
                    appendOrderToShareCollection(orderToShareCollection, order.getAdditionalDocument(), "Judiciary Outcome tab - %s");
                }
            });
        }

        List<DirectionOrderCollection> hearingOrderDocuments = data.getUploadHearingOrder();
        if (hearingOrderDocuments != null) {
            Collections.reverse(hearingOrderDocuments);
            hearingOrderDocuments.forEach(obj ->
                appendOrderToShareCollection(orderToShareCollection, obj.getValue().getUploadDraftDocument(),
                    "Case documents tab [Approved Order] - %s"));
        }

        populateProcessedAgreedDraftOrderToOrdersToShare(data, orderToShareCollection);
        populateFinalisedOrderToOrdersToShare(data, orderToShareCollection);

        if (data.getSendOrderWrapper().getOrdersToSend() == null) {
            data.getSendOrderWrapper().setOrdersToSend(OrdersToSend.builder().build());
        }
        data.getSendOrderWrapper().getOrdersToSend().setValue(orderToShareCollection);
    }

    private void populateProcessedAgreedDraftOrderToOrdersToShare(FinremCaseData data, List<OrderToShareCollection> orderToShareCollection) {
        emptyIfNull(data.getDraftOrdersWrapper().getAgreedDraftOrderCollection()).stream()
            .map(AgreedDraftOrderCollection::getValue)
            .filter(agreedDraftOrder -> PROCESSED == agreedDraftOrder.getOrderStatus())
            .forEach(agreedDraftOrder ->
                appendOrderToShareCollection(orderToShareCollection, agreedDraftOrder.getTargetDocument(), "Approved order - %s",
                    emptyIfNull(agreedDraftOrder.getAttachments()).stream().map(DocumentCollection::getValue).toArray(CaseDocument[]::new)
            ));
    }

    private void populateFinalisedOrderToOrdersToShare(FinremCaseData data, List<OrderToShareCollection> orderToShareCollection) {
        emptyIfNull(data.getDraftOrdersWrapper().getFinalisedOrdersCollection()).stream()
            .map(FinalisedOrderCollection::getValue)
            .forEach(finalisedOrder ->
                appendOrderToShareCollection(orderToShareCollection, finalisedOrder.getFinalisedDocument(), "Finalised order - %s",
                    emptyIfNull(finalisedOrder.getAttachments()).stream().map(DocumentCollection::getValue).toArray(CaseDocument[]::new)
                ));
    }

    private void appendOrderToShareCollection(List<OrderToShareCollection> orderToShareCollection,
                                              CaseDocument document, String format, CaseDocument... attachments) {
        OrderToShare.OrderToShareBuilder builder = OrderToShare.builder();
        builder.hasSupportingDocuments(YesOrNo.forValue(!isEmpty(attachments)));
        if (attachments != null) {
            List<AttachmentToShareCollection> attachmentElements = Arrays.stream(attachments)
                .map(attachment -> AttachmentToShareCollection.builder()
                    .value(AttachmentToShare.builder()
                        .documentId(getDocumentId(attachment))
                        .attachmentName(attachment.getDocumentFilename())
                        .documentToShare(YesOrNo.YES) // default to yes
                        .build())
                    .build())
                .toList();
            builder.attachmentsToShare(attachmentElements);
        }
        orderToShareCollection.add(OrderToShareCollection.builder().value(builder
            .documentId(getDocumentId(document))
            .documentName(String.format(format, document.getDocumentFilename()))
            .build()).build());
    }

    private int getCompareTo(ContestedGeneralOrderCollection e1, ContestedGeneralOrderCollection e2) {
        LocalDate e1Date = e1.getValue().getDateOfOrder() != null ? e1.getValue().getDateOfOrder() : LocalDate.now();
        LocalDate e2Date = e2.getValue().getDateOfOrder() != null ? e2.getValue().getDateOfOrder() : LocalDate.now();
        return e1Date.compareTo(e2Date);
    }

    private String getDocumentId(CaseDocument caseDocument) {
        String documentUrl = caseDocument.getDocumentUrl();
        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
    }

    public List<String> getParties(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        DynamicMultiSelectList parties = data.getPartiesOnCase();
        return parties.getValue().stream().map(DynamicMultiSelectListElement::getCode).toList();
    }

    public boolean isOrderSharedWithApplicant(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.APP_SOLICITOR.getCcdCode())
            || parties.contains(CaseRole.APP_BARRISTER.getCcdCode()));
    }

    public boolean isOrderSharedWithRespondent(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.RESP_SOLICITOR.getCcdCode())
            || parties.contains(CaseRole.RESP_BARRISTER.getCcdCode()));
    }

    public boolean isOrderSharedWithIntervener1(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_1.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_1.getCcdCode()));
    }

    public boolean isOrderSharedWithIntervener2(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_2.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_2.getCcdCode()));
    }

    public boolean isOrderSharedWithIntervener3(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_3.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_3.getCcdCode()));
    }

    public boolean isOrderSharedWithIntervener4(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_4.getCcdCode())
            || parties.contains(CaseRole.INTVR_SOLICITOR_4.getCcdCode()));
    }

    /**
     * Processes the selected hearing orders and categorizes them into legacy and new orders.
     *
     * <p>This method takes a list of selected orders and categorizes them into legacy or new
     * orders based on their type and status. It assumes that the provided {@code selectedOrders}
     * should have a value of 'Yes' in {@code documentToShare}; however, the filtering logic
     * applies regardless of this assumption.</p>
     *
     * @param caseDetails    the case details containing the case data
     * @param selectedOrders the list of orders selected to be shared
     * @return a pair containing two lists of case documents:
     *         - the first list contains legacy orders,
     *         - the second list contains new orders
     */
    public Pair<List<CaseDocument>, List<CaseDocument>> hearingOrdersToShare(FinremCaseDetails caseDetails, List<OrderToShare> selectedOrders) {
        FinremCaseData caseData = caseDetails.getData();
        final List<CaseDocument> legacyOrders = new ArrayList<>();
        final List<CaseDocument> newOrders = new ArrayList<>();

        List<DirectionOrderCollection> hearingOrders = caseData.getUploadHearingOrder();
        List<FinalisedOrderCollection> finalisedOrders = ofNullable(caseData.getDraftOrdersWrapper().getFinalisedOrdersCollection())
            .orElse(List.of());
        List<AgreedDraftOrderCollection> agreedDraftOrderCollection = emptyIfNull(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .stream().filter(a -> a.getValue().getOrderStatus() == PROCESSED).toList();

        if (selectedOrders != null) {
            if (selectedOrders.stream().anyMatch(s -> !YesOrNo.isYes(s.getDocumentToShare()))) {
                log.warn("It assumes that the provided selectedOrders should have a value of 'Yes' in documentToShare;"
                    + " however, this logic filters them regardless.");
            }
            selectedOrders.stream().filter(o -> YesOrNo.isYes(o.getDocumentToShare())).forEach(selected -> {
                boolean isProcessed = populateSelectedUploadHearingOrder(legacyOrders, hearingOrders, selected);
                if (!isProcessed) {
                    isProcessed = populateSelectedFinalisedOrders(newOrders, finalisedOrders, selected);
                }
                if (!isProcessed) {
                    populateSelectedProcessedOrders(newOrders, agreedDraftOrderCollection, selected);
                }
            });
        }
        return Pair.of(legacyOrders, newOrders);
    }

    public boolean isSelectedOrderMatches(List<OrderToShare> selectedDocs, ContestedGeneralOrder order) {
        if (order != null) {
            Optional<OrderToShare> listElement = selectedDocs.stream()
                .filter(e -> e.getDocumentId().equals(getDocumentId(order.getAdditionalDocument()))).findAny();
            return listElement.isPresent();
        }
        return false;
    }

    public void setPartiesToReceiveCommunication(FinremCaseDetails caseDetails, List<String> parties) {
        FinremCaseData data = caseDetails.getData();
        parties.forEach(role -> {
            data.setApplicantCorrespondenceEnabled(isOrderSharedWithApplicant(caseDetails));
            data.setRespondentCorrespondenceEnabled(isOrderSharedWithRespondent(caseDetails));
            data.getIntervenerOne()
                .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener1(caseDetails));
            data.getIntervenerTwo()
                .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener2(caseDetails));
            data.getIntervenerThree()
                .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener3(caseDetails));
            data.getIntervenerFour()
                .setIntervenerCorrespondenceEnabled(isOrderSharedWithIntervener4(caseDetails));
        });
    }

    private boolean populateSelectedUploadHearingOrder(List<CaseDocument> orders, List<DirectionOrderCollection> hearingOrders,
                                                       OrderToShare selected) {
        return addToOrders(selected, hearingOrders, hearingOrder -> hearingOrder.getValue().getUploadDraftDocument(), orders);
    }

    private boolean populateSelectedFinalisedOrders(List<CaseDocument> orders, List<FinalisedOrderCollection> finalisedOrders,
                                                    OrderToShare selected) {
        return populateSelectedOrdersWithAttachment(orders, finalisedOrders, finalisedOrder
            -> ((FinalisedOrder) finalisedOrder.getValue()).getFinalisedDocument(), selected);
    }

    private void populateSelectedProcessedOrders(List<CaseDocument> orders, List<AgreedDraftOrderCollection> agreedDraftOrderCollection,
                                                 OrderToShare selected) {
        populateSelectedOrdersWithAttachment(orders, agreedDraftOrderCollection, obj -> ((AgreedDraftOrder) obj.getValue()).getTargetDocument(),
            selected);
    }

    private boolean populateSelectedOrdersWithAttachment(List<CaseDocument> orders, List<? extends WithAttachmentsCollection> orderCollections,
                                                         Function<WithAttachmentsCollection, CaseDocument> documentExtractor, OrderToShare selected) {
        boolean ret = addToOrders(selected, orderCollections, documentExtractor, orders);

        if (selected.shouldIncludeSupportingDocuments()) {
            emptyIfNull(selected.getAttachmentsToShare()).stream()
                .map(AttachmentToShareCollection::getValue)
                .filter(this::isAttachmentSelected)
                .forEach(attachmentSelected -> addToOrders(attachmentSelected, orderCollections.stream()
                        .map(WithAttachmentsCollection::getValue)
                        .flatMap(d -> emptyIfNull(d.getAttachments()).stream())
                        .map(DocumentCollection::getValue).toList(),
                    d -> d, orders));
        }
        return ret;
    }

    private <T> boolean addToOrders(DocumentIdProvider selected, List<? extends T> collection, Function<T, CaseDocument> documentExtractor,
                                    List<CaseDocument> orders) {
        return emptyIfNull(collection).stream()
            .map(documentExtractor)
            .filter(Objects::nonNull)
            .filter(caseDocument -> getDocumentId(caseDocument).equals(selected.getDocumentId()))
            .findFirst()
            .map(orders::add)
            .orElse(false);
    }

    private boolean isAttachmentSelected(AttachmentToShare attachmentToShare) {
        return YesOrNo.isYes(attachmentToShare.getDocumentToShare());
    }
}
