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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPOND_TO_ORDER_DOCUMENTS;

@Component
public class DocumentHelper {

    public CaseDocument getLatestAmendedConsentOrder(Map<String, Object> caseData) {
        Optional<AmendedConsentOrderData> reduce = ofNullable(caseData.get(AMENDED_CONSENT_ORDER_COLLECTION))
                .map(this::convertToAmendedConsentOrderDataList)
                .orElse(emptyList())
                .stream()
                .reduce((first, second) -> second);
        return reduce
                .map(consentOrderData -> consentOrderData.getConsentOrder().getAmendedConsentOrder())
                .orElseGet(() -> convertToCaseDocument(caseData.get(LATEST_CONSENT_ORDER)));
    }

    public List<CaseDocument> getPensionDocumentsData(Map<String, Object> caseData) {
        return ofNullable(caseData.get(PENSION_DOCS_COLLECTION))
                .map(this::convertToPensionCollectionDataList)
                .orElse(emptyList())
                .stream()
                .map(PensionCollectionData::getPensionDocumentData)
                .map(PensionDocumentData::getPensionDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public CaseDocument convertToCaseDocument(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
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
                .filter(CommonConditions::isAmendedConsentOrderType)
                .reduce((first, second) -> second);
        if (respondToOrderData.isPresent()) {
            return respondToOrderData
                    .map(respondToOrderData1 -> respondToOrderData.get().getRespondToOrder().getDocumentLink());
        }

        return Optional.empty();
    }
}
