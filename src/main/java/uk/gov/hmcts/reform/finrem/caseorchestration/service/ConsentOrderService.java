package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@Service
@RequiredArgsConstructor
public class ConsentOrderService {

    private final DocumentHelper documentHelper;

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return getCaseDocument(caseDetails, callbackRequest.getEventId());
    }

    public Document getLatestConsentOrderData(FinremCallbackRequest callbackRequest) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        return getCaseDocument(caseDetails, callbackRequest.getEventType().getCcdType());
    }

    private CaseDocument getCaseDocument(CaseDetails caseDetails, String callbackRequest) {
        Map<String, Object> caseData = caseDetails.getData();
        String eventId = callbackRequest;
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                .orElseGet(() -> documentHelper.convertToCaseDocument(caseData.get(LATEST_CONSENT_ORDER)));
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return documentHelper.convertToCaseDocument(caseData.get(CONSENT_ORDER));
        }
    }

    private Document getCaseDocument(FinremCaseDetails caseDetails, String eventId) {
        FinremCaseData caseData = caseDetails.getData();
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                .orElseGet(() -> caseData.getLatestConsentOrder());
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return caseData.getConsentOrder();
        }
    }
}
