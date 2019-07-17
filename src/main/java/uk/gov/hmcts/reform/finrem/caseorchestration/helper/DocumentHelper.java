package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchFieldExistsException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
public class DocumentHelper {

    private static final String CONSENT_ORDER_COLLECTION = "amendedConsentOrderCollection";
    private static final String CONSENT_ORDER_MSG = "amendedConsentOrderCollection doesn't have documents in case_data";
    private static final String RESPOND_TO_ORDER_DOCUMENTS = "respondToOrderDocuments";
    private static final String DOCUMENT_DOESN_T_EXISTS = "AmendedConsentOrder type document doesn't exists.";
    private ObjectMapper objectMapper;
    private static final String AMENDED_CONSENT_ORDER = "AmendedConsentOrder";

    private static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }


    public CaseDocument getLatestAmendedConsentOrder(Map<String, Object> caseData) {
        return ofNullable(caseData.get(CONSENT_ORDER_COLLECTION))
                .map(this::convertToAmendedConsentOrderDataList)
                .orElseThrow(() -> new NoSuchFieldExistsException(CONSENT_ORDER_MSG))
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException(CONSENT_ORDER_MSG))
                .getConsentOrder()
                .getAmendedConsentOrder();
    }

    public List<CaseDocument> getPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get("pensionCollection"))
                .map(this::convertToPensionCollectionDataList)
                .orElseThrow(() -> new NoSuchFieldExistsException(CONSENT_ORDER_MSG))
                .stream()
                .map(PensionCollectionData::getPensionDocument)
                .map(PensionDocumentData::getPensionDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public CaseDocument convertToCaseDocument(Object object) {
        objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<AmendedConsentOrderData>>() {
        });
    }

    private List<PensionCollectionData> convertToPensionCollectionDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<PensionCollectionData>>() {
        });
    }


    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, new TypeReference<List<RespondToOrderData>>() {
        });
    }

    public CaseDocument getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        return ofNullable(caseData.get(RESPOND_TO_ORDER_DOCUMENTS))
                .map(this::convertToRespondToOrderDataList)
                .orElseThrow(() -> new NoSuchFieldExistsException(DOCUMENT_DOESN_T_EXISTS))
                .stream()
                .filter(DocumentHelper::isAmendedConsentOrderType)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchFieldExistsException(DOCUMENT_DOESN_T_EXISTS))
                .getRespondToOrder()
                .getDocumentLink();

    }
}
