package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ConsentOrderService {

    @Autowired
    private DocumentHelper documentHelper;

    private static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    private static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        Optional<CaseDocument> caseDocument;

        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
            caseDocument = documentHelper.getLatestRespondToOrderDocuments(caseData);
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return documentHelper.convertToCaseDocument(caseData.get("consentOrder"));
        }
        return caseDocument
                .orElseGet(() -> documentHelper.convertToCaseDocument(caseData.get("latestConsentOrder")));
    }
}
