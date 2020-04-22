package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

@Service
@Slf4j
public class ConsentOrderService {

    private static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    private static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";
    @Autowired
    private DocumentHelper documentHelper;

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        String eventId = callbackRequest.getEventId();
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                    .orElseGet(() -> documentHelper.convertToCaseDocument(caseData.get("latestConsentOrder")));
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return documentHelper.convertToCaseDocument(caseData.get("consentOrder"));
        }
    }
}