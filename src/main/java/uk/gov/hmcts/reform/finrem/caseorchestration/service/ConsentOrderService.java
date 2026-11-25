package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;

@Service
@RequiredArgsConstructor
public class ConsentOrderService {

    private final ConsentedApplicationHelper helper;
    private final BulkPrintDocumentService service;
    private final DocumentHelper documentHelper;

    public List<String> performCheck(CallbackRequest callbackRequest, String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Optional<Long> caseIdObj = Optional.ofNullable(caseDetails.getId());

        String caseId;
        if (caseIdObj.isPresent()) {
            caseId = String.valueOf(caseIdObj.get());
        } else {
            caseId = "Case not created yet.";
        }
        Map<String, Object> caseData = caseDetails.getData();

        helper.setConsentVariationOrderLabelField(caseData);
        List<String> errors = new ArrayList<>();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        Map<String, Object> beforeData = new HashMap<>();
        if (caseDetailsBefore != null) {
            beforeData = caseDetailsBefore.getData();
        }
        List<CaseDocument> caseDocuments = checkIfD81DocumentContainsEncryption(caseData, beforeData);
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            caseDocuments.forEach(document -> service.validateEncryptionOnUploadedDocument(document, caseId, errors, userAuthorisation));
        }
        return errors;
    }

    public CaseDocument getLatestConsentOrderData(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        return getCaseDocument(caseDetails, callbackRequest.getEventType().getCcdType());
    }

    private CaseDocument getCaseDocument(FinremCaseDetails caseDetails, String eventId) {
        FinremCaseData caseData = caseDetails.getData();
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                .orElseGet(caseData::getLatestConsentOrder);
        } else if (AMEND_CONSENT_ORDER.getCcdType().equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return caseData.getConsentOrder();
        }
    }

    public List<CaseDocument> checkIfD81DocumentContainsEncryption(Map<String, Object> caseData, Map<String, Object> beforeData) {
        List<CaseDocument> caseDocumentList = new ArrayList<>();

        CaseDocument consentOrderDocument = convertToCaseDocument(caseData.get("consentOrder"));
        if (consentOrderDocument != null) {
            caseDocumentList.add(consentOrderDocument);
        }

        setD81Document(caseData, caseDocumentList);
        setPensionDocuments(caseData, beforeData, caseDocumentList);
        setVariationOrderDocuments(caseData, beforeData, caseDocumentList);

        setOtherDocuments(caseData, beforeData, caseDocumentList);
        return caseDocumentList;
    }

    public void archiveAndUpdateLatestConsentOrder(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        FinremCaseData beforeData = callbackRequest.getFinremCaseDataBefore();

        CaseDocument newOrder = getLatestConsentOrderData(callbackRequest);
        CaseDocument previousOrder = beforeData.getLatestConsentOrder();

        //no new amendment is found
        if (previousOrder != null && Objects.equals(previousOrder.getDocumentUrl(), newOrder.getDocumentUrl())) {
            return;
        }

        if (previousOrder != null) {
            List<RespondToOrderDocumentCollection> archivedOrders =
                new ArrayList<>(Optional.ofNullable(caseData.getRespondToOrderDocuments())
                    .orElseGet(List::of));

            boolean alreadyArchived = archivedOrders.stream()
                .filter(Objects::nonNull)
                .map(RespondToOrderDocumentCollection::getValue)
                .filter(Objects::nonNull)
                .map(RespondToOrderDocument::getDocumentLink)
                .filter(Objects::nonNull)
                .anyMatch(document -> Objects.equals(
                    document.getDocumentUrl(), previousOrder.getDocumentUrl()));

            if (!alreadyArchived) {
                archivedOrders.add(RespondToOrderDocumentCollection.builder()
                    .value(RespondToOrderDocument.builder()
                        .documentType(RespondToOrderDocumentType.AMEND_CONSENT_ORDER)
                        .documentLink(new CaseDocument(previousOrder))
                        .documentFileName(previousOrder.getDocumentFilename())
                        .documentDateAdded(getPreviousConsentOrderDate(beforeData, previousOrder))
                        .build())
                    .build());
            }

            caseData.setRespondToOrderDocuments(archivedOrders);
        }

        caseData.setLatestConsentOrder(newOrder);
    }

    private LocalDate getPreviousConsentOrderDate(FinremCaseData beforeData,
                                                  CaseDocument previousOrder) {

        Optional<LocalDate> amendedOrderDate =
            Optional.ofNullable(beforeData.getAmendedConsentOrderCollection())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(AmendedConsentOrderCollection::getValue)
                .filter(Objects::nonNull)
                .filter(order -> order.getAmendedConsentOrder() != null
                    && Objects.equals(
                    order.getAmendedConsentOrder().getDocumentUrl(),
                    previousOrder.getDocumentUrl()))
                .map(AmendedConsentOrder::getAmendedConsentOrderDate)
                .filter(Objects::nonNull)
                .findFirst();

        if (amendedOrderDate.isPresent()) {
            return amendedOrderDate.get();
        }

        return previousOrder.getUploadTimestamp() != null
            ? previousOrder.getUploadTimestamp().toLocalDate()
            : null;
    }

    private void setOtherDocuments(Map<String, Object> caseData, Map<String, Object> beforeData, List<CaseDocument> caseDocumentList) {
        List<CaseDocument> otherDocumentsData = new ArrayList<>(documentHelper.getConsentOrderOtherDocumentsData(caseData));
        if (!otherDocumentsData.isEmpty()) {
            if (!beforeData.isEmpty()) {
                List<CaseDocument> otherDocumentsDataBefore = documentHelper.getConsentOrderOtherDocumentsData(beforeData);
                if (otherDocumentsDataBefore != null && !otherDocumentsDataBefore.isEmpty()) {
                    otherDocumentsData.removeAll(otherDocumentsDataBefore);
                }
            }
            caseDocumentList.addAll(otherDocumentsData);
        }

    }

    private void setVariationOrderDocuments(Map<String, Object> caseData, Map<String, Object> beforeData, List<CaseDocument> caseDocumentList) {
        List<CaseDocument> variationOrderDocumentsData = new ArrayList<>(documentHelper.getVariationOrderDocumentsData(caseData));
        if (!variationOrderDocumentsData.isEmpty()) {
            if (!beforeData.isEmpty()) {
                List<CaseDocument> variationOrderDocumentsDataBefore = documentHelper.getVariationOrderDocumentsData(beforeData);
                if (variationOrderDocumentsDataBefore != null && !variationOrderDocumentsDataBefore.isEmpty()) {
                    variationOrderDocumentsData.removeAll(variationOrderDocumentsDataBefore);
                }
            }
            caseDocumentList.addAll(variationOrderDocumentsData);
        }
    }

    private void setPensionDocuments(Map<String, Object> caseData, Map<String, Object> beforeData, List<CaseDocument> caseDocumentList) {
        List<CaseDocument> pensionDocumentsData = new ArrayList<>(documentHelper.getPensionDocumentsData(caseData));
        if (!pensionDocumentsData.isEmpty()) {
            if (!beforeData.isEmpty()) {
                List<CaseDocument> pensionDocumentsDataBefore = documentHelper.getPensionDocumentsData(beforeData);
                if (pensionDocumentsDataBefore != null && !pensionDocumentsDataBefore.isEmpty()) {
                    pensionDocumentsData.removeAll(pensionDocumentsDataBefore);
                }
            }
            caseDocumentList.addAll(pensionDocumentsData);
        }
    }

    private void setD81Document(Map<String, Object> caseData, List<CaseDocument> caseDocumentList) {
        Object d81Question = caseData.get("d81Question");
        if (d81Question != null) {
            if (d81Question.equals("Yes")) {
                CaseDocument caseDocument = convertToCaseDocument(caseData.get("d81Joint"));
                if (caseDocument != null) {
                    caseDocumentList.add(caseDocument);
                }
            } else {
                CaseDocument caseAppDocument = convertToCaseDocument(caseData.get("d81Applicant"));
                if (caseAppDocument != null) {
                    caseDocumentList.add(caseAppDocument);
                }
                CaseDocument caseRespDocument = convertToCaseDocument(caseData.get("d81Respondent"));
                if (caseRespDocument != null) {
                    caseDocumentList.add(caseRespDocument);
                }
            }
        }
    }

    private CaseDocument convertToCaseDocument(Object object) {
        if (object == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(object, CaseDocument.class);
    }
}
