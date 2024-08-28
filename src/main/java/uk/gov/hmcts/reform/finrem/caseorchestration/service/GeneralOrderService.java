package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderAddressTo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderPreviewDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.GeneralOrderDocumentCategoriser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_BODY_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralOrderService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final CaseDataService caseDataService;
    private final PartyService partyService;
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
            caseData.put("GeneralOrderCourt", ContestedCourtHelper.getSelectedCourt(caseDetails));
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
            Optional.ofNullable(generalOrderWrapper.getGeneralOrderCollection())
                .orElse(new ArrayList<>());
        generalOrders.add(item);
        generalOrderWrapper.setGeneralOrderCollection(generalOrders);
    }

    public void addContestedGeneralOrderToCollection(FinremCaseData caseData) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders =
            Optional.ofNullable(generalOrderWrapper.getGeneralOrders())
                .orElse(new ArrayList<>());
        generalOrderWrapper.setGeneralOrders(generalOrders);

        updateContestedGeneralOrders(caseData, generalOrders);
    }

    public void addConsentedInContestedGeneralOrderToCollection(FinremCaseData caseData) {
        GeneralOrderWrapper generalOrderWrapper = caseData.getGeneralOrderWrapper();
        List<ContestedGeneralOrderCollection> generalOrders =
            Optional.ofNullable(generalOrderWrapper.getGeneralOrdersConsent())
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
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();

        List<ContestedGeneralOrderCollection> generalOrders = data.getGeneralOrderWrapper().getGeneralOrders();

        if (generalOrders != null && !generalOrders.isEmpty()) {
            generalOrders.sort(Comparator.nullsLast(this::getCompareTo));
            generalOrders.forEach(generalOrder -> {
                ContestedGeneralOrder order = generalOrder.getValue();
                if (order != null && order.getAdditionalDocument() != null) {
                    String filename = order.getAdditionalDocument().getDocumentFilename();
                    String documentId = getDocumentId(order.getAdditionalDocument());
                    dynamicListElements.add(partyService.getDynamicMultiSelectListElement(documentId,
                        "Judiciary Outcome tab" + " - " + filename));
                }
            });
        }

        List<DirectionOrderCollection> hearingOrderDocuments = data.getUploadHearingOrder();
        if (hearingOrderDocuments != null) {
            Collections.reverse(hearingOrderDocuments);
            hearingOrderDocuments.forEach(obj -> {
                CaseDocument document = obj.getValue().getUploadDraftDocument();
                dynamicListElements.add(partyService.getDynamicMultiSelectListElement(getDocumentId(document),
                    "Case documents tab [Approved Order]" + " - " + document.getDocumentFilename()));
            });
        }

        DynamicMultiSelectList dynamicOrderList = getDynamicOrderList(dynamicListElements, new DynamicMultiSelectList());
        data.setOrdersToShare(dynamicOrderList);
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

    private DynamicMultiSelectList getDynamicOrderList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                                       DynamicMultiSelectList selectedOrders) {
        if (selectedOrders != null && selectedOrders.getValue() != null) {
            return DynamicMultiSelectList.builder()
                .value(selectedOrders.getValue())
                .listItems(dynamicMultiSelectListElement)
                .build();
        } else {
            return DynamicMultiSelectList.builder()
                .listItems(dynamicMultiSelectListElement)
                .build();
        }
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

    public List<CaseDocument> hearingOrdersToShare(FinremCaseDetails caseDetails, DynamicMultiSelectList selectedDocs) {
        FinremCaseData caseData = caseDetails.getData();
        List<CaseDocument> orders = new ArrayList<>();
        List<DirectionOrderCollection> hearingOrders = caseData.getFinalOrderCollection();
        if (selectedDocs != null && hearingOrders != null) {
            List<DynamicMultiSelectListElement> docs = selectedDocs.getValue();
            docs.forEach(doc -> hearingOrders.forEach(obj -> addToList(doc, obj, orders, caseDetails.getId())));
        }
        return orders;
    }

    private void addToList(DynamicMultiSelectListElement doc, DirectionOrderCollection obj,
                           List<CaseDocument> orders, Long caseId) {
        if (getDocumentId(obj.getValue().getUploadDraftDocument()).equals(doc.getCode())) {
            CaseDocument caseDocument = obj.getValue().getUploadDraftDocument();
            log.info("Adding document to orders {} for Case ID: {}", caseDocument, caseId);
            orders.add(caseDocument);
        }
    }

    public boolean isSelectedOrderMatches(DynamicMultiSelectList selectedDocs, ContestedGeneralOrder order) {
        if (order != null) {
            Optional<DynamicMultiSelectListElement> listElement = selectedDocs.getValue().stream()
                .filter(e -> e.getCode().equals(getDocumentId(order.getAdditionalDocument()))).findAny();
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
}