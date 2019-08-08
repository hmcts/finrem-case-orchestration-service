package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.translateOrderRefusalCollection;

@Service
public class RefusalOrderDocumentService extends AbstractDocumentService {

    private static final String DOCUMENT_COMMENT = "System Generated";

    private Function<Pair<CaseDetails, String>, CaseDocument> generateDocument = this::applyGenerateRefusalOrder;
    private Function<CaseDocument, ConsentOrderData> createConsentOrderData = this::applyCreateConsentOrderData;

    @Autowired
    public RefusalOrderDocumentService(DocumentClient documentClient,
                                       DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public Map<String, Object> generateConsentOrderNotApproved(
            String authorisationToken, final CaseDetails caseDetails) {
        return translateOrderRefusalCollection
                .andThen(generateDocument)
                .andThen(createConsentOrderData)
                .andThen(consentOrderData -> populateConsentOrderData(consentOrderData, caseDetails))
                .apply(Pair.of(copyOf(caseDetails), authorisationToken));
    }

    public Map<String, Object> previewConsentOrderNotApproved(
            String authorisationToken, final CaseDetails caseDetails) {
        return translateOrderRefusalCollection
                .andThen(generateDocument)
                .andThen(caseDocument -> populateConsentOrderNotApproved(caseDocument, caseDetails))
                .apply(Pair.of(copyOf(caseDetails), authorisationToken));
    }

    private Map<String, Object> populateConsentOrderNotApproved(CaseDocument caseDocument, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("orderRefusalPreviewDocument", caseDocument);
        return caseData;
    }


    private Map<String, Object> populateConsentOrderData(ConsentOrderData consentOrderData, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        List<ConsentOrderData> uploadOrder = Optional.ofNullable(caseData.get("uploadOrder"))
                .map(this::convertToUploadOrderList)
                .orElse(new ArrayList<>());
        uploadOrder.add(consentOrderData);
        caseData.put("uploadOrder", uploadOrder);
        return caseData;
    }

    private CaseDocument applyGenerateRefusalOrder(Pair<CaseDetails, String> data) {
        return generateDocument(data.getRight(), data.getLeft(),
                config.getRejectedOrderTemplate(),
                config.getRejectedOrderFileName());
    }

    private ConsentOrderData applyCreateConsentOrderData(CaseDocument caseDocument) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(config.getRejectedOrderDocType());
        consentOrder.setDocumentDateAdded(new Date());
        consentOrder.setDocumentLink(caseDocument);
        consentOrder.setDocumentComment(DOCUMENT_COMMENT);

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(UUID.randomUUID().toString());
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    private List<ConsentOrderData> convertToUploadOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<ConsentOrderData>>() {
        });
    }
}
