package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchFieldExistsException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class ConsentOrderService {

    private ObjectMapper objectMapper;

    private static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return "AmendedConsentOrder".equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        try {
            if ("FR_respondToOrder".equalsIgnoreCase(callbackRequest.getEventId())) {
                return getLatestRespondToOrderDocuments(caseData);
            } else if ("FR_amendedConsentOrder".equalsIgnoreCase(callbackRequest.getEventId())) {
                return getLatestAmendedConsentOrder(caseData);
            } else {
                return convertToCaseDocument(caseData.get("consentOrder"));
            }
        } catch (NoSuchFieldExistsException ex) {
            log.info(ex.getMessage());
            return convertToCaseDocument(caseData.get("latestConsentOrder"));
        }
    }

    private CaseDocument getLatestAmendedConsentOrder(Map<String, Object> caseData) {
       return ofNullable(caseData.get("amendedConsentOrderCollection"))
                .map(this::convertToAmendedConsentOrderDataList)
                .get()
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException(
                        "amendedConsentOrderCollection doesn't have documents in case_data"))
                .getConsentOrder()
                .getAmendedConsentOrder();
    }


    private CaseDocument convertToCaseDocument(Object object) {
        objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<AmendedConsentOrderData>>() {
        });
    }

    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<RespondToOrderData>>() {
        });
    }

    private CaseDocument getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        return ofNullable(caseData.get("respondToOrderDocuments"))
                .map(this::convertToRespondToOrderDataList)
                .get()
                .stream()
                .filter(ConsentOrderService::isAmendedConsentOrderType)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException("AmendedConsentOrder type document doesn't exists."))
                .getRespondToOrder()
                .getDocumentLink();

    }
}
