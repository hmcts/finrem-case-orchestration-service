package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderRefusalTranslator.translateOrderRefusalCollection;

@Service
public class DocumentService {

    private static final String DOCUMENT_CASE_DETAILS_JSON_KEY = "caseDetails";
    private static final String ORDER_MADE_STATE = "orderMade";
    private static final String DOCUMENT_COMMENT = "System Generated";

    private final DocumentConfiguration documentConfiguration;
    private final DocumentGeneratorClient documentGeneratorClient;
    private final ObjectMapper objectMapper;

    private Function<Pair<CaseDetails, String>, CaseDocument> generateDocument = this::applyGenerateRefusalOrder;
    private Function<CaseDocument, ConsentOrderData> createConsentOrderData = this::applyCreateConsentOrderData;
    private UnaryOperator<Map<String, Object>> updateCaseStateToOrderMade = this::updateState;

    @Autowired
    public DocumentService(DocumentGeneratorClient documentGeneratorClient,
                           DocumentConfiguration documentConfiguration,
                           ObjectMapper objectMapper) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.documentConfiguration = documentConfiguration;
        this.objectMapper = objectMapper;
    }

    public CaseDocument generateMiniFormA(String authorisationToken, CaseDetails caseDetails) {
        return generateDocument(authorisationToken, caseDetails,
                documentConfiguration.getMiniFormTemplate(),
                documentConfiguration.getMiniFormFileName());
    }

    public Map<String, Object> generateConsentOrderNotApproved(
            String authorisationToken, final CaseDetails caseDetails) {
        return translateOrderRefusalCollection
                .andThen(generateDocument)
                .andThen(createConsentOrderData)
                .andThen(consentOrderData -> populateConsentOrderData(consentOrderData, caseDetails))
                .andThen(updateCaseStateToOrderMade)
                .apply(ImmutablePair.of(copyOf(caseDetails), authorisationToken));
    }

    private Map<String, Object> updateState(Map<String, Object> caseData) {
        caseData.put("state", ORDER_MADE_STATE);
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

    private CaseDocument generateDocument(String authorisationToken, CaseDetails caseDetails,
                                          String template, String fileName) {
        Document miniFormA =
                documentGeneratorClient.generatePDF(
                        DocumentRequest.builder()
                                .template(template)
                                .fileName(fileName)
                                .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                                .build(),
                        authorisationToken);

        return caseDocument(miniFormA);
    }

    private CaseDocument applyGenerateRefusalOrder(Pair<CaseDetails, String> data) {
        return generateDocument(data.getRight(), data.getLeft(),
                documentConfiguration.getRejectedOrderTemplate(),
                documentConfiguration.getRejectedOrderFileName());
    }

    private ConsentOrderData applyCreateConsentOrderData(CaseDocument caseDocument) {
        ConsentOrder consentOrder = new ConsentOrder();
        consentOrder.setDocumentType(documentConfiguration.getRejectedOrderDocType());
        consentOrder.setDocumentDateAdded(new Date());
        consentOrder.setDocumentLink(caseDocument);
        consentOrder.setDocumentComment(DOCUMENT_COMMENT);

        ConsentOrderData consentOrderData = new ConsentOrderData();
        consentOrderData.setId(UUID.randomUUID().toString());
        consentOrderData.setConsentOrder(consentOrder);

        return consentOrderData;
    }

    private CaseDocument caseDocument(Document miniFormA) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(miniFormA.getBinaryUrl());
        caseDocument.setDocumentFilename(miniFormA.getFileName());
        caseDocument.setDocumentUrl(miniFormA.getUrl());
        return caseDocument;
    }

    private CaseDetails copyOf(CaseDetails caseDetails) {
        try {
            return objectMapper
                    .readValue(objectMapper.writeValueAsString(caseDetails), CaseDetails.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    private List<ConsentOrderData> convertToUploadOrderList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<List<ConsentOrderData>>() {});
    }

    public Map<String, Object> generateCourtCoverSheet(String authorisationToken, CaseDetails caseDetails) {
        throw new UnsupportedOperationException();
    }
}
