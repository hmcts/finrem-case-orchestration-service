package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
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

    public CaseDocument getLatestConsentOrderData(FinremCallbackRequest callbackRequest) {
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

    private CaseDocument getCaseDocument(FinremCaseDetails caseDetails, String eventId) {
        FinremCaseData caseData = caseDetails.getData();
        if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestRespondToOrderDocuments(caseData)
                .orElseGet(caseData::getLatestConsentOrder);
        } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(eventId)) {
            return documentHelper.getLatestAmendedConsentOrder(caseData);
        } else {
            return caseData.getConsentOrder();
        }
    }


    public List<CaseDocument> checkIfD81DocumentContainsEncryption(Map<String, Object> caseData) {
        List<CaseDocument> caseDocumentList = new ArrayList<>();

        CaseDocument consentOrderDocument = convertToCaseDocument(caseData.get("consentOrder"));
        if (consentOrderDocument != null) {
            caseDocumentList.add(consentOrderDocument);
        }

        setD81Document(caseData, caseDocumentList);

        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(caseData);
        if (pensionDocumentsData != null && !pensionDocumentsData.isEmpty()) {
            caseDocumentList.addAll(pensionDocumentsData);
        }

        List<CaseDocument> variationOrderDocumentsData = documentHelper.getVariationOrderDocumentsData(caseData);
        if (variationOrderDocumentsData != null && !variationOrderDocumentsData.isEmpty()) {
            caseDocumentList.addAll(variationOrderDocumentsData);
        }

        List<CaseDocument> otherDocumentsData = documentHelper.getConsentOrderOtherDocumentsData(caseData);
        if (otherDocumentsData != null && !otherDocumentsData.isEmpty()) {
            caseDocumentList.addAll(otherDocumentsData);
        }

        return caseDocumentList;
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
