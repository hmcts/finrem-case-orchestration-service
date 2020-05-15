package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

public class OnlineFormDocumentServiceTest {

    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();
    private GenericDocumentService genericDocumentService;
    private OnlineFormDocumentService onlineFormDocumentService;
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
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(new CountDownLatch(1)), mapper);
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateContestedMiniFormA() {
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(new CountDownLatch(1)), mapper);
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateContestedDraftMiniFormA() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(latch), mapper);
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));

        CaseDocument result = onlineFormDocumentService.generateDraftContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().data(caseData()).build());
        latch.await(30, TimeUnit.SECONDS);

        assertThat(latch.getCount(), is(0L));
        assertCaseDocument(result);
    }

    private Map<String, Object> caseData() {
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("document_url", "http://test.url");

        Map<String, Object> data = new HashMap<>();
        data.put(MINI_FORM_A, documentMap);

        return data;
    }

    private static class DocumentClientStub implements DocumentClient {

        private final CountDownLatch latch;

        DocumentClientStub(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Document generatePdf(DocumentGenerationRequest generateDocumentRequest, String authorizationToken) {
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
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken, String fileUrl) {
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
