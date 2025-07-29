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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_RESPOND_TO_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

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

    public List<String> performCheck(FinremCallbackRequest callbackRequest, String userAuthorisation, FinremCaseDetailsMapper finremCaseDetailsMapper) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        Optional<Long> caseIdObj = Optional.ofNullable(finremCaseDetails.getId());

        String caseId;
        if (caseIdObj.isPresent()) {
            caseId = String.valueOf(caseIdObj.get());
        } else {
            caseId = "Case not created yet.";
        }
        FinremCaseData finremcaseData = finremCaseDetails.getData();

        helper.setConsentVariationOrderLabelField(finremcaseData);
        List<String> errors = new ArrayList<>();

        Map<String, Object> beforeData = Optional.ofNullable(callbackRequest.getCaseDetailsBefore())
            .map(finremCaseDetailsMapper::mapToCaseDetails)
            .map(CaseDetails::getData)
            .orElseGet(HashMap::new);

        // TODO: This caused NPT - Put in a random state. Remove this once the state is set in the callback
        finremCaseDetails.setState(State.CASE_ADDED);
        Map<String, Object> caseData = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails).getData();
        List<CaseDocument> caseDocuments = checkIfD81DocumentContainsEncryption(caseData, beforeData);
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            caseDocuments.forEach(document -> service.validateEncryptionOnUploadedDocument(document, caseId, errors, userAuthorisation));
        }
        return errors;
    }

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
