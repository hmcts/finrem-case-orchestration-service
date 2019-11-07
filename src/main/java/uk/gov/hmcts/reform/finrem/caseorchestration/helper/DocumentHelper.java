package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Component
public class DocumentHelper {

    private static final String CONSENT_ORDER_COLLECTION = "amendedConsentOrderCollection";
    private static final String RESPOND_TO_ORDER_DOCUMENTS = "respondToOrderDocuments";
    private ObjectMapper objectMapper;
    private static final String AMENDED_CONSENT_ORDER = "AmendedConsentOrder";

    private static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }


    public CaseDocument getLatestAmendedConsentOrder(Map<String, Object> caseData) {
        Optional<AmendedConsentOrderData> reduce = ofNullable(caseData.get(CONSENT_ORDER_COLLECTION))
                .map(this::convertToAmendedConsentOrderDataList)
                .orElse(emptyList())
                .stream()
                .reduce((first, second) -> second);
        return reduce
                .map(consentOrderData -> consentOrderData.getConsentOrder().getAmendedConsentOrder())
                .orElseGet(() -> convertToCaseDocument(caseData.get("latestConsentOrder")));


    }

    public List<CaseDocument> getPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get("pensionCollection"))
                .map(this::convertToPensionCollectionDataList)
                .orElse(emptyList())
                .stream()
                .map(PensionCollectionData::getPensionDocumentData)
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

    public Optional<CaseDocument> getLatestRespondToOrderDocuments(Map<String, Object> caseData) {
        Optional<RespondToOrderData> respondToOrderData = ofNullable(caseData.get(RESPOND_TO_ORDER_DOCUMENTS))
                .map(this::convertToRespondToOrderDataList)
                .orElse(emptyList())
                .stream()
                .filter(DocumentHelper::isAmendedConsentOrderType)
                .reduce((first, second) -> second);
        if (respondToOrderData.isPresent()) {
            return respondToOrderData
                    .map(respondToOrderData1 -> respondToOrderData.get().getRespondToOrder().getDocumentLink());
        }
        return Optional.empty();

    }
}
