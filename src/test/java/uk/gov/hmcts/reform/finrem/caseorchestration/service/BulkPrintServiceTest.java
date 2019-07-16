package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class BulkPrintServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private DocumentClient generatorClient;
    private DocumentConfiguration config;
    private BulkPrintService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setBulkPrintFileName("test_file");
        config.setBulkPrintTemplate("test_template");

        generatorClient = new TestDocumentClient();
        service = new BulkPrintService(generatorClient, config, mapper);
    }

    @Test
    public void generateGeneralLetter() throws Exception {
        service.sendForBulkPrint(caseDetails(), AUTH_TOKEN);
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
              getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private class TestDocumentClient implements DocumentClient {

        private Map<String, Object> value;

        @Override
        public Document generatePDF(DocumentRequest request, String authorizationToken) {
            value = request.getValues();
            return document();
        }

        @Override
        public void bulkPrint(BulkPrintRequest bulkPrintRequest) {
            assertThat(bulkPrintRequest.getBulkPrintDocuments().size(), is(2));
            assertThat(
                ((CaseDetails) value.get("caseDetails")).getId().toString(),
                is(bulkPrintRequest.getCaseId()));
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}
