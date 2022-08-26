package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.RespondToOrderDocumentType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@Service
@RequiredArgsConstructor
public class ConsentOrderService {

    private final DocumentHelper documentHelper;

    public CaseDocument getLatestConsentOrderData(OldCallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        String eventId = callbackRequest.getEventId();
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                .orElseGet(() -> documentHelper.convertToCaseDocument(caseData.get(LATEST_CONSENT_ORDER)));
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return documentHelper.convertToCaseDocument(caseData.get(CONSENT_ORDER));
        }
    }

    public Document getLatestConsentOrderData(CallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        FinremCaseData caseData = caseDetails.getCaseData();
        EventType eventType = callbackRequest.getEventType();

        if (EventType.RESPOND_TO_ORDER.equals(eventType)) {
            return getLatestRespondToOrderDocuments(caseData).orElseGet(caseData::getLatestConsentOrder);
        } else if (EventType.AMENDED_CONSENT_ORDER.equals(eventType)) {
            return getLatestAmendedConsentOrder(caseData);
        }

        return caseData.getConsentOrder();
    }

    public Optional<Document> getLatestRespondToOrderDocuments(FinremCaseData caseData) {
        Optional<RespondToOrderDocumentCollection> respondToOrderDocumentCollection =
            Optional.ofNullable(caseData.getRespondToOrderDocuments()).orElse(new ArrayList<>())
                .stream()
                .filter(document -> RespondToOrderDocumentType.AMENDED_CONSENT_ORDER.equals(document.getValue().getDocumentType()))
                .reduce((first, second) -> second);

        if (respondToOrderDocumentCollection.isPresent()) {
            return respondToOrderDocumentCollection
                .map(respondToOrderData1 -> respondToOrderDocumentCollection.get().getValue().getDocumentLink());
        }

        return Optional.empty();
    }

    public Document getLatestAmendedConsentOrder(FinremCaseData caseData) {
        return ofNullable(caseData.getAmendedConsentOrderCollection()).orElse(emptyList()).stream()
            .reduce((first, second) -> second)
            .map(consentOrderData -> consentOrderData.getValue().getAmendedConsentOrder())
            .orElseGet(caseData::getLatestConsentOrder);
    }

}
