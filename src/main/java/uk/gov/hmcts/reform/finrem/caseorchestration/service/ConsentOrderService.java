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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String eventId = callbackRequest.getEventType().getCcdType();
        FinremCaseData caseData = caseDetails.getData();

        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return getLatestAmendedOrderUploadedInThisEvent(caseData, callbackRequest.getFinremCaseDataBefore())
                .orElseGet(caseData::getLatestConsentOrder);
        } else if (AMEND_CONSENT_ORDER.getCcdType().equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return caseData.getConsentOrder();
        }
    }

    /**
     * Respond To Order: only an amended consent order uploaded in this event can become the latest order.
     * Entries already present before the event (earlier uploads, or orders archived by
     * {@link #archiveAndUpdateLatestConsentOrder}) are ignored, so they can never be re-promoted.
     */
    private Optional<CaseDocument> getLatestAmendedOrderUploadedInThisEvent(FinremCaseData caseData,
                                                                            FinremCaseData beforeData) {
        Set<String> urlsBefore = respondToOrderDocumentLinks(
            beforeData == null ? null : beforeData.getRespondToOrderDocuments())
            .map(CaseDocument::getDocumentUrl)
            .collect(Collectors.toSet());

        return Optional.ofNullable(caseData.getRespondToOrderDocuments())
            .orElseGet(List::of)
            .stream()
            .filter(Objects::nonNull)
            .map(RespondToOrderDocumentCollection::getValue)
            .filter(Objects::nonNull)
            .filter(document -> RespondToOrderDocumentType.AMEND_CONSENT_ORDER.equals(document.getDocumentType()))
            .map(RespondToOrderDocument::getDocumentLink)
            .filter(Objects::nonNull)
            .filter(link -> !urlsBefore.contains(link.getDocumentUrl()))
            .reduce((first, second) -> second);
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

    /**
     * Sets the latest consent order for the current event and, when it has changed, archives the
     * previous latest order into Respond To Order Documents (DFR-4981).
     * Used by both upload routes: Amended Consent Order (caseworker) and Respond To Order (solicitor).
     */
    public void archiveAndUpdateLatestConsentOrder(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        FinremCaseData beforeData = callbackRequest.getFinremCaseDataBefore();

        CaseDocument newOrder = getLatestConsentOrderData(callbackRequest);
        CaseDocument previousOrder = beforeData == null ? null : beforeData.getLatestConsentOrder();

        if (newOrder == null) {
            // nothing to promote - never clear an existing latest order
            return;
        }

        if (previousOrder != null && !isSameDocument(previousOrder, newOrder)) {
            archivePreviousOrder(caseData, beforeData, previousOrder);
        }

        caseData.setLatestConsentOrder(newOrder);
    }

    private void archivePreviousOrder(FinremCaseData caseData, FinremCaseData beforeData, CaseDocument previousOrder) {
        List<RespondToOrderDocumentCollection> respondToOrderDocuments =
            new ArrayList<>(Optional.ofNullable(caseData.getRespondToOrderDocuments()).orElseGet(List::of));

        // e.g. the previous order was itself uploaded via Respond To Order - it is already in the history
        boolean alreadyArchived = respondToOrderDocumentLinks(respondToOrderDocuments)
            .anyMatch(link -> isSameDocument(link, previousOrder));
        if (alreadyArchived) {
            return;
        }

        respondToOrderDocuments.add(RespondToOrderDocumentCollection.builder()
            .value(RespondToOrderDocument.builder()
                .documentType(RespondToOrderDocumentType.AMEND_CONSENT_ORDER)
                .documentLink(new CaseDocument(previousOrder))
                .documentFileName(previousOrder.getDocumentFilename())
                .documentDateAdded(getPreviousConsentOrderDate(beforeData, previousOrder))
                .build())
            .build());
        caseData.setRespondToOrderDocuments(respondToOrderDocuments);
    }

    private LocalDate getPreviousConsentOrderDate(FinremCaseData beforeData, CaseDocument previousOrder) {
        return findAmendedConsentOrder(beforeData, previousOrder)
            .map(AmendedConsentOrder::getAmendedConsentOrderDate)
            .or(() -> Optional.ofNullable(previousOrder.getUploadTimestamp()).map(LocalDateTime::toLocalDate))
            .orElseGet(LocalDate::now);
    }

    private Optional<AmendedConsentOrder> findAmendedConsentOrder(FinremCaseData beforeData, CaseDocument document) {
        return Optional.ofNullable(beforeData.getAmendedConsentOrderCollection())
            .orElseGet(List::of)
            .stream()
            .filter(Objects::nonNull)
            .map(AmendedConsentOrderCollection::getValue)
            .filter(Objects::nonNull)
            .filter(order -> order.getAmendedConsentOrder() != null
                && isSameDocument(order.getAmendedConsentOrder(), document))
            .findFirst();
    }

    private static Stream<CaseDocument> respondToOrderDocumentLinks(List<RespondToOrderDocumentCollection> documents) {
        return Optional.ofNullable(documents)
            .orElseGet(List::of)
            .stream()
            .filter(Objects::nonNull)
            .map(RespondToOrderDocumentCollection::getValue)
            .filter(Objects::nonNull)
            .map(RespondToOrderDocument::getDocumentLink)
            .filter(Objects::nonNull);
    }

    private static boolean isSameDocument(CaseDocument first, CaseDocument second) {
        return Objects.equals(first.getDocumentUrl(), second.getDocumentUrl());
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
