package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContestedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_PREVIEW_DOCUMENT;


public class GeneralOrderServiceTest {
    private DocumentClient generatorClient;
    private ObjectMapper mapper = new ObjectMapper();
    private GenericDocumentService genericDocumentService;
    private GeneralOrderService generalOrderService;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setGeneralOrderTemplate("test_template");
        config.setGeneralOrderFileName("test_file");

        generatorClient = new GeneralOrderServiceTest.TestDocumentClient();
        genericDocumentService = new GenericDocumentService(generatorClient);
        generalOrderService = new GeneralOrderService(genericDocumentService, config, new DocumentHelper(mapper), mapper);
    }

    @Test
    public void generateGeneralOrder() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedCaseDetails());

        GeneralOrder result = (GeneralOrder) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result.getGeneralOrder());
        ((GeneralOrderServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFields();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(contestedCaseDetails());

        List<GeneralOrderContestedData> generalOrders = (List<GeneralOrderContestedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONTESTED);
        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getId(), is("1234"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getId(), notNullValue());
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void submitConsentedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(consentedCaseDetails());
        List<GeneralOrderConsentedData> generalOrders = (List<GeneralOrderConsentedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED);
        assertThat(generalOrders, hasSize(2));
        assertThat(generalOrders.get(0).getId(), is("1234"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(generalOrders.get(0).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(generalOrders.get(1).getId(), notNullValue());
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(generalOrders.get(1).getGeneralOrder().getGeneralOrder().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    private CaseDetails consentedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-consented.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private CaseDetails contestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-contested.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
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

        void verifyAdditionalFields() {
            Map<String, Object> data = data();
            assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}
