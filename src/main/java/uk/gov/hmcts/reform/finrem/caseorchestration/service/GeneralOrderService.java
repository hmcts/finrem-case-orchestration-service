package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContestedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderPreviewDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_BODY_TEXT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
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
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private Function<CaseDocument, GeneralOrderPreviewDocument> createGeneralOrderData = this::applyGeneralOrderData;
    private UnaryOperator<CaseDetails> addExtraFields = this::applyAddExtraFields;
    private BiFunction<CaseDetails, String, CaseDocument> generateDocument = this::applyGenerateDocument;

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
            documentConfiguration.getGeneralOrderFileName());
    }

    private GeneralOrderPreviewDocument applyGeneralOrderData(CaseDocument caseDocument) {
        return new GeneralOrderPreviewDocument(caseDocument);
    }

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

    public Map<String, Object> populateGeneralOrderCollection(CaseDetails caseDetails) {
        caseDetails.getData().put(GENERAL_ORDER_LATEST_DOCUMENT,
            documentHelper.convertToCaseDocument(caseDetails.getData().get(GENERAL_ORDER_PREVIEW_DOCUMENT)));
        if (caseDataService.isConsentedApplication(caseDetails)) {
            return populateGeneralOrderCollectionConsented(caseDetails);
        } else {
            return populateGeneralOrderCollectionContested(caseDetails);
        }
    }

    private Map<String, Object> populateGeneralOrderCollectionConsented(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        GeneralOrderConsented generalOrder =
            new GeneralOrderConsented(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER_PREVIEW_DOCUMENT)),
                getAddressToFormatted(caseData));

        GeneralOrderConsentedData consentedData = new GeneralOrderConsentedData(UUID.randomUUID().toString(), generalOrder);

        List<GeneralOrderConsentedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONSENTED))
            .map(this::convertToGeneralOrderConsentedList)
            .orElse(new ArrayList<>());
        generalOrderList.add(consentedData);

        caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED, generalOrderList);
        return caseData;
    }

    private Map<String, Object> populateGeneralOrderCollectionContested(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();

        GeneralOrderContested generalOrder =
            new GeneralOrderContested(documentHelper.convertToCaseDocument(caseData.get(GENERAL_ORDER_PREVIEW_DOCUMENT)),
                getAddressToFormatted(caseData));

        GeneralOrderContestedData contestedData = new GeneralOrderContestedData(UUID.randomUUID().toString(), generalOrder);

        if (caseDataService.isConsentedInContestedCase(caseDetails)) {
            List<GeneralOrderContestedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED))
                .map(this::convertToGeneralOrderContestedList)
                .orElse(new ArrayList<>());
            generalOrderList.add(contestedData);

            caseData.put(GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED, generalOrderList);

        } else {
            List<GeneralOrderContestedData> generalOrderList = Optional.ofNullable(caseData.get(GENERAL_ORDER_COLLECTION_CONTESTED))
                .map(this::convertToGeneralOrderContestedList)
                .orElse(new ArrayList<>());
            generalOrderList.add(contestedData);

            caseData.put(GENERAL_ORDER_COLLECTION_CONTESTED, generalOrderList);
        }

        return caseData;
    }

    private List<GeneralOrderConsentedData> convertToGeneralOrderConsentedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralOrderConsentedData>>() {
        });
    }

    private List<GeneralOrderContestedData> convertToGeneralOrderContestedList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<GeneralOrderContestedData>>() {
        });
    }

    private String getAddressToFormatted(Map<String, Object> caseData) {
        String storedValue = String.valueOf(caseData.get(GENERAL_ORDER_ADDRESS_TO));
        if ("applicant".equals(storedValue)) {
            return "Applicant";
        } else if ("applicantSolicitor".equals(storedValue)) {
            return "Applicant Solicitor";
        } else if ("respondentSolicitor".equals(storedValue)) {
            return "Respondent Solicitor";
        } else {
            return "";
        }
    }

    public void setOrderList(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();
        List<DirectionOrderCollection> hearingOrderDocuments = data.getUploadHearingOrder();

        if (hearingOrderDocuments != null) {
            hearingOrderDocuments.forEach(obj -> dynamicListElements.add(getDynamicMultiSelectListElement(obj.getId(),
                "Case documents tab [Approved Order]" + " - " + obj.getValue().getUploadDraftDocument().getDocumentFilename())));
        }

        if (ObjectUtils.isNotEmpty(data.getGeneralOrderWrapper().getGeneralOrderLatestDocument())) {
            CaseDocument orderLatestDocument = data.getGeneralOrderWrapper().getGeneralOrderLatestDocument();
            String orderLatestDocumentFilename = orderLatestDocument.getDocumentFilename();
            dynamicListElements.add(getDynamicMultiSelectListElement(orderLatestDocumentFilename,
                "Orders tab [Lastest general order]" + " - " + orderLatestDocumentFilename));
        }

        DynamicMultiSelectList selectedOrders = data.getOrdersToShare();

        DynamicMultiSelectList dynamicOrderList = getDynamicOrderList(dynamicListElements, selectedOrders);
        data.setOrdersToShare(dynamicOrderList);
    }

    public DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
            .code(code)
            .label(label)
            .build();
    }

    private DynamicMultiSelectList getDynamicOrderList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                                           DynamicMultiSelectList selectedOrders) {
        if (selectedOrders != null) {
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
        return (parties.contains(CaseRole.APP_SOLICITOR.getValue())
            || parties.contains(CaseRole.APP_BARRISTER.getValue()));
    }

    public boolean isOrderSharedWithRespondent(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.RESP_SOLICITOR.getValue())
            || parties.contains(CaseRole.RESP_BARRISTER.getValue()));
    }

    public boolean isOrderSharedWithIntervener1(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_1.getValue())
            || parties.contains(CaseRole.INTVR_SOLICITOR_1.getValue()));
    }

    public boolean isOrderSharedWithIntervener2(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_2.getValue())
            || parties.contains(CaseRole.INTVR_SOLICITOR_2.getValue()));
    }

    public boolean isOrderSharedWithIntervener3(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_3.getValue())
            || parties.contains(CaseRole.INTVR_SOLICITOR_3.getValue()));
    }

    public boolean isOrderSharedWithIntervener4(FinremCaseDetails caseDetails) {
        List<String> parties = getParties(caseDetails);
        return (parties.contains(CaseRole.INTVR_BARRISTER_4.getValue())
            || parties.contains(CaseRole.INTVR_SOLICITOR_4.getValue()));
    }

    public List<CaseDocument> hearingOrdersToShare(FinremCaseDetails caseDetails, DynamicMultiSelectList selectedDocs) {
        FinremCaseData caseData = caseDetails.getData();
        List<CaseDocument> orders = new ArrayList<>();
        List<DirectionOrderCollection> hearingOrders = caseData.getUploadHearingOrder();
        if (selectedDocs != null && hearingOrders != null) {
            List<DynamicMultiSelectListElement> docs = selectedDocs.getValue();
            docs.forEach(doc -> hearingOrders.forEach(obj -> addToList(doc, obj, orders, caseDetails.getId())));
        }
        return orders;
    }

    private void addToList(DynamicMultiSelectListElement doc, DirectionOrderCollection obj,
                           List<CaseDocument> orders, Long caseId) {
        if (obj.getId().equals(doc.getCode())) {
            CaseDocument caseDocument = obj.getValue().getUploadDraftDocument();
            log.info("Adding document to orders {} for caseId {}", caseDocument, caseId);
            orders.add(caseDocument);
        }
    }

    public List<BulkPrintDocument> getBulkPrintDocuments(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        DynamicMultiSelectList selectedOrders = caseData.getOrdersToShare();

        List<BulkPrintDocument>  bulkPrintDocuments = new ArrayList<>();
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (isSelectedOrderMatches(selectedOrders, generalOrder)) {
            bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(generalOrder));
        }

        List<CaseDocument> hearingOrders = hearingOrdersToShare(caseDetails, selectedOrders);
        if (!hearingOrders.isEmpty()) {
            hearingOrders.forEach(doc -> bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(doc)));
        }

        CaseDocument document = caseData.getAdditionalDocument();
        if (document != null) {
            bulkPrintDocuments.add(documentHelper.getCaseDocumentAsBulkPrintDocument(document));
        }
        return bulkPrintDocuments;
    }

    public boolean isSelectedOrderMatches(DynamicMultiSelectList selectedDocs, CaseDocument caseDocument) {
        if (caseDocument != null) {
            Optional<DynamicMultiSelectListElement> listElement = selectedDocs.getValue().stream()
                .filter(e -> e.getCode().equals(caseDocument.getDocumentFilename())).findAny();
            return listElement.isPresent();
        }
        return false;
    }
}