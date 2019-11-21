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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class GenerateCoverSheetServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private DocumentClient generatorClient;
    private DocumentConfiguration config;
    private GenerateCoverSheetService coverSheetService;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setBulkPrintFileName("test_file");
        config.setBulkPrintTemplate("test_template");

        generatorClient = new TestDocumentClient();
        coverSheetService = new GenerateCoverSheetService(generatorClient, config, mapper);
    }

    @Test
    public void shouldGenerateRespondentCoverSheet() throws Exception {
        CaseDocument caseDocument = coverSheetService.generateRespondentCoverSheet(caseDetails(), "AUTH_TOKEN");
        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));
    }

    @Test
    public void shouldGenerateApplicantCoverSheet() throws Exception {
        CaseDocument caseDocument = coverSheetService.generateApplicantCoverSheet(caseDetails(), "AUTH_TOKEN");
        assertThat(document().getBinaryUrl(), is(caseDocument.getDocumentBinaryUrl()));
        assertThat(document().getFileName(), is(caseDocument.getDocumentFilename()));
        assertThat(document().getUrl(), is(caseDocument.getDocumentUrl()));
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream("/fixtures/bulk-print.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private class TestDocumentClient implements DocumentClient {


        @Override
        public Document generatePdf(DocumentGenerationRequest request, String authorizationToken) {
            assertThat(request.getTemplate(), is("test_template"));
            return document();
        }

        @Override
        public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken, String fileUrl,
                                                                int contentLength) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Document stampDocument(Document document, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Document annexStampDocument(Document document, String authorizationToken) {
            throw new UnsupportedOperationException();
        }


    }
}
