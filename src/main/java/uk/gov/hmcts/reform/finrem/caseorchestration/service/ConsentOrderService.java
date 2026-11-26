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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.RESPOND_TO_ORDER;

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

    /**
     * Resolves which consent order should be the latest consent order for the current event.
     *
     * <ul>
     *     <li><b>Respond To Order</b>: the last amended consent order uploaded by the solicitor in this event.
     *     If none was uploaded, the existing latest consent order is kept.</li>
     *     <li><b>Amended Consent Order</b>: the last order in the amended consent order collection.</li>
     *     <li><b>Any other event</b> (e.g. case creation or amend application): the draft consent order.</li>
     * </ul>
     *
     * @param callbackRequest the CCD callback request for the current event
     * @return the consent order that should be the latest, or {@code null} if none can be resolved
     */
    public CaseDocument getLatestConsentOrderData(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String eventId = callbackRequest.getEventType().getCcdType();
        FinremCaseData caseData = caseDetails.getData();

        if (RESPOND_TO_ORDER.getCcdType().equalsIgnoreCase(eventId)) {
            return getLatestAmendedOrderUploadedInThisEvent(caseData, callbackRequest.getFinremCaseDataBefore())
                .orElseGet(caseData::getLatestConsentOrder);
        } else if (AMEND_CONSENT_ORDER.getCcdType().equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return caseData.getConsentOrder();
        }
    }

    /**
     * Finds the last amended consent order uploaded in Respond To Order Documents during the current event.
     *
     * <p>Documents that were already in the collection before the event (earlier solicitor uploads, or previous
     * orders added by {@link #archiveAndUpdateLatestConsentOrder}) are ignored, so they can never become the
     * latest consent order again. Documents are matched on their document URL.</p>
     *
     * @param caseData   case data after the event
     * @param beforeData case data before the event; if {@code null}, every document is treated as new
     * @return the last newly uploaded amended consent order, or empty if none was uploaded in this event
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
     * Updates the latest consent order for the current event and keeps the order it replaces as history (DFR-4981).
     *
     * <p>Used by both routes for uploading a new consent order after refusal: Amended Consent Order (caseworker)
     * and Respond To Order (solicitor). Behaviour:</p>
     * <ul>
     *     <li>If no order can be resolved for the event, nothing changes. The latest consent order is never cleared.</li>
     *     <li>If the resolved order is the same document as the previous latest order, only the latest consent
     *     order is set. Nothing is added to the history.</li>
     *     <li>Otherwise the previous latest order is added to Respond To Order Documents (unless it is already
     *     there), and the new order becomes the latest consent order.</li>
     * </ul>
     *
     * @param callbackRequest the CCD callback request; its case data is updated in place
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

    /**
     * Adds the previous latest consent order to the end of Respond To Order Documents, so the history stays in
     * the order the versions were replaced.
     *
     * <p>Nothing is added if the order is already in the collection, for example when the solicitor uploaded
     * it there in an earlier Respond To Order event. The archived entry holds a copy of the document reference,
     * so it is independent of the latest consent order field.</p>
     *
     * @param caseData      case data after the event; its Respond To Order Documents are updated
     * @param beforeData    case data before the event, used to find the order's amended date
     * @param previousOrder the latest consent order before this event
     */
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

    /**
     * Works out the date to show against an archived consent order.
     *
     * <p>Uses the first of:</p>
     * <ol>
     *     <li>the amended consent order date entered by the caseworker, if the order is in the amended
     *     consent order collection</li>
     *     <li>the date the document was uploaded</li>
     *     <li>today's date, which is when it was archived</li>
     * </ol>
     *
     * @param beforeData    case data before the event
     * @param previousOrder the order being archived
     * @return the date to show; never {@code null}
     */
    private LocalDate getPreviousConsentOrderDate(FinremCaseData beforeData, CaseDocument previousOrder) {
        return findAmendedConsentOrder(beforeData, previousOrder)
            .map(AmendedConsentOrder::getAmendedConsentOrderDate)
            .or(() -> Optional.ofNullable(previousOrder.getUploadTimestamp()).map(LocalDateTime::toLocalDate))
            .orElseGet(LocalDate::now);
    }

    /**
     * Finds the entry in the amended consent order collection that holds the given document, matched on document URL.
     *
     * @param beforeData case data before the event
     * @param document   the document to look for
     * @return the matching amended consent order, or empty if the document is not an amended consent order
     */
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

    /**
     * Returns the document links in a Respond To Order Documents collection, skipping null entries.
     *
     * @param documents the collection; may be {@code null}
     * @return a stream of the non-null document links, in collection order
     */
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
