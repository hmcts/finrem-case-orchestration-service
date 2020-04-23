package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@Service
@Slf4j
public class ConsentOrderService {

    @Autowired
    private DocumentHelper documentHelper;

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
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
}