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

    private static final String FR_AMENDED_CONSENT_ORDER = "FR_amendedConsentOrder";
    private static final String FR_RESPOND_TO_ORDER = "FR_respondToOrder";
    private static final String CONSENT_ORDER_COLLECTION = "amendedConsentOrderCollection";
    private static final String CONSENT_ORDER_MSG = "amendedConsentOrderCollection doesn't have documents in case_data";
    private static final String RESPOND_TO_ORDER_DOCUMENTS = "respondToOrderDocuments";
    private static final String DOCUMENT_DOESN_T_EXISTS = "AmendedConsentOrder type document doesn't exists.";
    private static final String AMENDED_CONSENT_ORDER = "AmendedConsentOrder";
    private ObjectMapper objectMapper = new ObjectMapper();

    private static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }

    public CaseDocument getLatestConsentOrderData(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        try {
            if (FR_RESPOND_TO_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
                return getLatestRespondToOrderDocuments(caseData);
            } else if (FR_AMENDED_CONSENT_ORDER.equalsIgnoreCase(callbackRequest.getEventId())) {
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
        return ofNullable(caseData.get(CONSENT_ORDER_COLLECTION))
                .map(this::convertToAmendedConsentOrderDataList)
                .orElseThrow(() -> new NoSuchFieldExistsException(CONSENT_ORDER_MSG))
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException(CONSENT_ORDER_MSG))
                .getConsentOrder()
                .getAmendedConsentOrder();
    }


    private CaseDocument convertToCaseDocument(Object object) {
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<AmendedConsentOrderData>>() {
        });
    }

    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<RespondToOrderData>>() {
        });
    }

    private CaseDocument getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        return ofNullable(caseData.get(RESPOND_TO_ORDER_DOCUMENTS))
                .map(this::convertToRespondToOrderDataList)
                .orElseThrow(() -> new NoSuchFieldExistsException(DOCUMENT_DOESN_T_EXISTS))
                .stream()
                .filter(ConsentOrderService::isAmendedConsentOrderType)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException(DOCUMENT_DOESN_T_EXISTS))
                .getRespondToOrder()
                .getDocumentLink();

    }
}
