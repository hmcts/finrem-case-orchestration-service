package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.REJECTED_ORDER_TYPE;

public class RefusalOrderDocumentServiceTest {

    private DocumentGeneratorClient generatorClient;
    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private RefusalOrderDocumentService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setRejectedOrderTemplate("test_template");
        config.setRejectedOrderFileName("test_file");
        config.setRejectedOrderDocType(REJECTED_ORDER_TYPE);

        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);

        generatorClient = Mockito.mock(DocumentGeneratorClient.class);
        when(generatorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document);

        service = new RefusalOrderDocumentService(generatorClient, config, mapper);
    }

    @Test
    public void generateConsentOrderNotApproved() throws Exception {
        CaseDetails caseDetails = caseDetails();

        Map<String, Object> caseData = service.generateConsentOrderNotApproved(AUTH_TOKEN, caseDetails);
        ConsentOrderData consentOrderData = consentOrderData(caseData);

        assertThat(caseData.get("state"), is(equalTo("orderMade")));
        assertThat(consentOrderData.getId(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentType(), is(REJECTED_ORDER_TYPE));
        assertThat(consentOrderData.getConsentOrder().getDocumentDateAdded(), is(notNullValue()));
        assertThat(consentOrderData.getConsentOrder().getDocumentComment(), is(equalTo("System Generated")));

        doCaseDocumentAssert(consentOrderData.getConsentOrder().getDocumentLink());
    }

    private ConsentOrderData consentOrderData(Map<String, Object> caseData) {
        List<ConsentOrderData> list =
                mapper.convertValue(caseData.get("uploadOrder"), new TypeReference<List<ConsentOrderData>>() {});

        return list
                .stream()
                .filter(cd -> cd.getConsentOrder().getDocumentType().equals(REJECTED_ORDER_TYPE))
                .findFirst().orElseThrow(() -> new IllegalStateException(REJECTED_ORDER_TYPE + " missing"));
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/model/case-details.json")) {
            return mapper.readValue(resourceAsStream, CaseDetails.class);
        }
    }
}