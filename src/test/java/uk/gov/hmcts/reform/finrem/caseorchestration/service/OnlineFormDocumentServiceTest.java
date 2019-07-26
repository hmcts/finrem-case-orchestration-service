package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

public class OnlineFormDocumentServiceTest {

    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private OnlineFormDocumentService service;

    private OptionIdToValueTranslator translator;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setMiniFormTemplate("template");
        config.setMiniFormFileName("file_name");
        translator = new OptionIdToValueTranslator("/options/options-id-value-transform.json",
                new ObjectMapper());
        translator.initOptionValueMap();
    }

    @Test
    public void generateMiniFormA() {
        service = new OnlineFormDocumentService(new DocumentClientStub(new CountDownLatch(1)),
                config, translator, mapper);
        doCaseDocumentAssert(service.generateMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateContestedMiniFormA() {
        service = new OnlineFormDocumentService(new DocumentClientStub(new CountDownLatch(1)),
                config, translator, mapper);
        doCaseDocumentAssert(service.generateContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateContestedDraftMiniFormA() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        service = new OnlineFormDocumentService(new DocumentClientStub(latch), config, translator, mapper);

        CaseDocument result =
                service.generateDraftContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().data(caseData()).build());
        latch.await();

        doCaseDocumentAssert(result);
    }

    private Map<String, Object> caseData() {
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("document_url", "http://test.url");

        Map<String, Object> data = new HashMap<>();
        data.put("miniFormA", documentMap);

        return data;
    }

    private static class DocumentClientStub implements DocumentClient {

        private final CountDownLatch latch;

        DocumentClientStub(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Document generatePDF(DocumentGenerationRequest generateDocumentRequest, String authorizationToken) {
            latch.countDown();
            return document();
        }

        @Override
        public UUID bulkPrint(BulkPrintRequest bulkPrintRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            latch.countDown();
        }

        @Override
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken,
                                                                String fileUrl) {
            return  DocumentValidationResponse.builder().build();
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