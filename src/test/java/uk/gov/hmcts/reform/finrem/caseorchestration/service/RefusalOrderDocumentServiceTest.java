package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.REJECTED_ORDER_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_PREVIEW_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;

public class RefusalOrderDocumentServiceTest {

    private ObjectMapper mapper = new ObjectMapper();
    private RefusalOrderDocumentService service;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setRejectedOrderTemplate("test_template");
        config.setRejectedOrderFileName("test_file");
        config.setRejectedOrderDocType(REJECTED_ORDER_TYPE);

        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);

        DocumentClient generatorClient = Mockito.mock(DocumentClient.class);
        when(generatorClient.generatePdf(isA(DocumentGenerationRequest.class), eq(AUTH_TOKEN))).thenReturn(document);

        service = new RefusalOrderDocumentService(generatorClient, config, mapper);
    }

    @Test
    public void generateConsentOrderNotApproved() throws Exception {
        CaseDetails caseDetails = caseDetails("/fixtures/model/case-details.json");

        Map<String, Object> caseData = service.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        ConsentOrderData consentOrderData = consentOrderData(caseData);

        assertThat(consentOrderData.getId(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentType(), is(REJECTED_ORDER_TYPE));
        assertThat(consentOrderData.getConsentOrder().getDocumentDateAdded(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentComment(), is(equalTo("System Generated")));

        assertCaseDocument(consentOrderData.getConsentOrder().getDocumentLink());
    }

    @Test
    public void multipleRefusalOrdersGenerateConsentOrderNotApproved() throws Exception {
        CaseDetails caseDetails = caseDetails("/fixtures/model/copy-case-details-multiple-orders.json");

        Map<String, Object> caseData = service.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        List<OrderRefusalData> orderRefusalData = refusalOrderCollection(caseData);
        assertThat(orderRefusalData.size(), is(2));
        assertThat(orderRefusalData.get(0).getId(), Is.is("1"));
        assertThat(orderRefusalData.get(1).getId(), Is.is("1"));

        ConsentOrderData consentOrderData = consentOrderData(caseData);
        assertThat(consentOrderData.getId(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentType(), is(REJECTED_ORDER_TYPE));
        assertThat(consentOrderData.getConsentOrder().getDocumentDateAdded(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentComment(), is(equalTo("System Generated")));

        assertCaseDocument(consentOrderData.getConsentOrder().getDocumentLink());
    }

    @Test
    public void previewConsentOrderNotApproved() throws Exception {
        CaseDetails caseDetails = caseDetails("/fixtures/model/case-details.json");
        Map<String, Object> caseData = service.previewConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        CaseDocument caseDocument = getCaseDocument(caseData);

        assertCaseDocument(caseDocument);
    }

    private CaseDocument getCaseDocument(Map<String, Object> caseData) {
        Object orderRefusalPreviewDocument = caseData.get(ORDER_REFUSAL_PREVIEW_COLLECTION);

        return mapper.convertValue(orderRefusalPreviewDocument, CaseDocument.class);
    }

    private ConsentOrderData consentOrderData(Map<String, Object> caseData) {
        List<ConsentOrderData> list =
                mapper.convertValue(caseData.get(UPLOAD_ORDER), new TypeReference<List<ConsentOrderData>>() {
                });

        return list
                .stream()
                .filter(cd -> cd.getConsentOrder().getDocumentType().equals(REJECTED_ORDER_TYPE))
                .findFirst().orElseThrow(() -> new IllegalStateException(REJECTED_ORDER_TYPE + " missing"));
    }

    private CaseDetails caseDetails(String name) throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(name)) {
            return mapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }

    private List<OrderRefusalData> refusalOrderCollection(Map<String, Object> caseData) {
        return mapper.convertValue(caseData.get(ORDER_REFUSAL_COLLECTION),
                new TypeReference<List<OrderRefusalData>>() {
                });
    }
}