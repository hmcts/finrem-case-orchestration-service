package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
    public static final String ORDER_MADE_STATE = "orderMade";
    public static final String DOCUMENT_COMMENT = "System Generated";

    private final DocumentConfiguration documentConfiguration;
    private final DocumentGeneratorClient documentGeneratorClient;
    private final ObjectMapper objectMapper;

    private Function<Pair<CaseDetails, String>, CaseDocument>
            generateDocument = this::applyGenerateConsentOrderNotApproved;

    private Function<CaseDocument, Map<String, Object>> createConsentOrderData = this::applyCreateConsentOrderData;
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

    public Map<String, Object> generateConsentOrderNotApproved(String authorisationToken, final CaseDetails caseDetails) {
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

    private Map<String, Object> populateConsentOrderData(Map<String, Object> consentOrderData, CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        List<ConsentOrderData> data = Optional.ofNullable(caseData.getUploadOrder()).orElse(new ArrayList<>());
        caseData.setUploadOrder(data);

        caseData.getUploadOrder().add(consentOrderData);
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

    private CaseDocument applyGenerateConsentOrderNotApproved(Pair<CaseDetails, String> data) {
        return generateDocument(data.getRight(), data.getLeft(),
                documentConfiguration.getRejectedOrderTemplate(),
                documentConfiguration.getRejectedOrderFileName());
    }

    private Map<String, Object> applyCreateConsentOrderData(CaseDocument caseDocument) {
        Map<String, Object> consentOrder = new HashMap<>();
        consentOrder.put("documentType", documentConfiguration.getRejectedOrderDocType());
        consentOrder.put("documentDateAdded", new Date());
        consentOrder.put("documentLink", caseDocument);
        consentOrder.put("documentComment", DOCUMENT_COMMENT);

        Map<String, Object> consentOrderData = new HashMap<>();
        consentOrderData.put("id", UUID.randomUUID().toString());
        consentOrderData.put("consentOrder", consentOrder);
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
}
