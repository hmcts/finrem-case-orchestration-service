package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class BulkPrintServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private DocumentClient generatorClient;
    private DocumentConfiguration config;
    private BulkPrintService service;
    private UUID letterId;

    @Before
    public void setUp() {
        letterId = UUID.randomUUID();
        config = new DocumentConfiguration();
        config.setBulkPrintFileName("test_file");
        config.setBulkPrintTemplate("test_template");

        generatorClient = new TestDocumentClient();
        service = new BulkPrintService(generatorClient, config, mapper);
    }

    @Test
    public void sendForBulkPrint() throws Exception {
        UUID bulkPrintLetterId = service.sendForBulkPrint(new CaseDocument(), caseDetails());
        assertThat(letterId, is(bulkPrintLetterId));

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
        public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
            assertThat(bulkPrintRequest.getBulkPrintDocuments().size(), is(5));
            return letterId;
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken,
                                                                String fileUrl) {
            throw new UnsupportedOperationException();
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}
