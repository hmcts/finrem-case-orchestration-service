package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_AUTHORISATION_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_3B;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_NATURE_OF_APPLICATION_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

public class OnlineFormDocumentServiceTest {

    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();
    private GenericDocumentService genericDocumentService;
    private OnlineFormDocumentService onlineFormDocumentService;
    private OptionIdToValueTranslator translator;

    private DocumentClient generatorClient;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setMiniFormTemplate("template");
        config.setMiniFormFileName("file_name");
        translator = new OptionIdToValueTranslator("/options/options-id-value-transform.json",
                new ObjectMapper());
        translator.initOptionValueMap();
        generatorClient = new OnlineFormDocumentServiceTest.TestDocumentClient();
    }

    @Test
    public void generateMiniFormA() {
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(new CountDownLatch(1)));
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));
        assertCaseDocument(onlineFormDocumentService.generateMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateContestedMiniFormA() {
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(new CountDownLatch(1)));
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));
        assertCaseDocument(onlineFormDocumentService.generateContestedMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }

    @Test
    public void generateConsentedInContestedMiniFormA() throws Exception {
        generatorClient = new TestDocumentClient();
        genericDocumentService = new GenericDocumentService(generatorClient);
        onlineFormDocumentService = new OnlineFormDocumentService(genericDocumentService, config, translator, new DocumentHelper(mapper));

        CaseDetails caseDetails = consentedInContestedCaseDetails();
        CaseDocument miniFormA = onlineFormDocumentService.generateConsentedInContestedMiniFormA(caseDetails, AUTH_TOKEN);
        doCaseDocumentAssert(miniFormA);

        ((OnlineFormDocumentServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFields();
    }

    @Test
    public void generateContestedDraftMiniFormA() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        genericDocumentService = new GenericDocumentService(new DocumentClientStub(latch));
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

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
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

    private CaseDetails consentedInContestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/mini-form-a-consent-in-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private static class TestDocumentClient implements DocumentClient {

        private Map<String, Object> value;

        @Override
        public Document generatePdf(DocumentGenerationRequest request, String authorizationToken) {
            this.value = request.getValues();
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
        public DocumentValidationResponse checkUploadedFileType(String authorizationToken, String fileUrl) {
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

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }

        void verifyAdditionalFields() {
            Map<String, Object> data = data();

            //Solicitor Details
            assertThat(data.get(CONSENTED_SOLICITOR_NAME), is("Solicitor"));
            assertThat(data.get(CONSENTED_SOLICITOR_FIRM), is("Awesome Firm"));

            assertThat(data, IsMapContaining.hasKey(CONSENTED_SOLICITOR_ADDRESS));
            Map<String, Object> addressObject = (Map<String, Object>)data.get(CONSENTED_SOLICITOR_ADDRESS);
            assertThat(addressObject.get("County").toString(), is("County"));
            assertThat(addressObject.get("Country").toString(), is("UK"));
            assertThat(addressObject.get("PostCode").toString(), is("SW1A 1AA"));
            assertThat(addressObject.get("PostTown").toString(), is("London"));
            assertThat(addressObject.get("AddressLine1").toString(), is("Buckingham Palace"));
            assertThat(addressObject.get("AddressLine2").toString(), is("null"));
            assertNull(addressObject.get("AddressLine3"));

            //Respondent Details
            assertThat(data.get(CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME), is("john"));
            assertThat(data.get(CONSENTED_RESPONDENT_LAST_NAME), is("smith"));
            assertThat(data.get(CONSENTED_RESPONDENT_REPRESENTED), is("No"));

            //Checklist
            assertThat(data, IsMapContaining.hasKey(CONSENTED_NATURE_OF_APPLICATION));
            assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(0).toString(), is("Periodical Payment Order"));
            assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(1).toString(), is("Lump Sum Order"));
            assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION)).get(2).toString(), is("Property Adjustment Order"));

            assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3A), is("test"));
            assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_3B), is("test"));

            //Order For Children Reasons
            assertThat(data.get(CONSENTED_ORDER_FOR_CHILDREN), is("Yes"));
            assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_5), is("No"));

            assertThat(data, IsMapContaining.hasKey(CONSENTED_NATURE_OF_APPLICATION_6));
            assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(0).toString(), is("item1"));
            assertThat(((ArrayList) data.get(CONSENTED_NATURE_OF_APPLICATION_6)).get(1).toString(), is("item2"));

            assertThat(data.get(CONSENTED_NATURE_OF_APPLICATION_7), is("test"));

            assertThat(data.get(CONSENTED_AUTHORISATION_FIRM), is("Authorised Firm"));
        }
    }
}