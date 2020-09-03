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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderConsentedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderContestedData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
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
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_COLLECTION_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
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
    public void generateGeneralOrderConsented() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);
        ((GeneralOrderServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFieldsConsented();
    }

    @Test
    public void generateGeneralOrderContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, contestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);
        ((GeneralOrderServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFieldsContested();
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
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = (CaseDocument)documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT);
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void generateGeneralOrderConsentedInContested() throws Exception {
        Map<String, Object> documentMap = generalOrderService.createGeneralOrder(AUTH_TOKEN, consentedInContestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(GENERAL_ORDER_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);
        ((GeneralOrderServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFieldsContested();
    }

    @Test
    public void submitConsentedInContestedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(consentedInContestedCaseDetails());

        List<GeneralOrderContestedData> generalOrders = (List<GeneralOrderContestedData>)documentMap.get(
            GENERAL_ORDER_COLLECTION_CONSENTED_IN_CONTESTED);
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
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = (CaseDocument)documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT);
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
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
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant"));

        CaseDocument latestGeneralOrder = (CaseDocument)documentMap.get(GENERAL_ORDER_LATEST_DOCUMENT);
        assertThat(latestGeneralOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestGeneralOrder.getDocumentFilename(),
            is("WhatsApp Image 2018-07-24 at 3.05.39 PM.jpeg"));
        assertThat(latestGeneralOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void addressToFormattedCorrectlyForApplicant() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_ADDRESS_TO, "applicant");
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderConsentedData> generalOrders = (List<GeneralOrderConsentedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED);
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant"));
    }

    @Test
    public void addressToFormattedCorrectlyForApplicantSolicitor() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_ADDRESS_TO, "applicantSolicitor");
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderConsentedData> generalOrders = (List<GeneralOrderConsentedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED);
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Applicant Solicitor"));
    }

    @Test
    public void addressToFormattedCorrectlyForRespondentSolicitor() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_ADDRESS_TO, "respondentSolicitor");
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderConsentedData> generalOrders = (List<GeneralOrderConsentedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED);
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is("Respondent Solicitor"));
    }

    @Test
    public void addressToFormattedCorrectlyReturnsEmptyStringForInvalid() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_ADDRESS_TO, "invalid");
        Map<String, Object> documentMap = generalOrderService.populateGeneralOrderCollection(details);
        List<GeneralOrderConsentedData> generalOrders = (List<GeneralOrderConsentedData>)documentMap.get(GENERAL_ORDER_COLLECTION_CONSENTED);
        assertThat(generalOrders.get(1).getGeneralOrder().getAddressTo(), is(""));
    }

    @Test
    public void getsCorrectGeneralOrdersForPrintingConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData());
        assertThat(latestGeneralOrder.getBinaryFileUrl(), is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    @Test
    public void getsZeroGeneralOrdersForPrintingWhenNoneConsented() throws Exception {
        CaseDetails details = consentedCaseDetails();
        details.getData().put(GENERAL_ORDER_LATEST_DOCUMENT, null);
        BulkPrintDocument latestGeneralOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(details.getData());
        assertTrue(latestGeneralOrder == null);
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

    private CaseDetails consentedInContestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-order-consented-in-contested.json")) {
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

        void verifyAdditionalFieldsConsented() {
            Map<String, Object> data = data();

            assertThat(data.get("DivorceCaseNumber"), is("DD12D12345"));
            assertThat(data.get("ApplicantName"), is("Consented Applicant Name"));
            assertThat(data.get("RespondentName"), is("Consented Respondent Name"));
            assertThat(data.get("GeneralOrderCourt"), is("SITTING in private"));
            assertThat(data.get("GeneralOrderJudgeDetails"), is("His Honour Judge Consented"));
            assertThat(data.get("GeneralOrderRecitals"), is("Consented Recitals"));
            assertThat(data.get("GeneralOrderDate"), is("01/01/2020"));
            assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for consented"));
            assertThat(data.get("GeneralOrderHeaderOne"), is("Sitting in the Family Court"));
        }

        void verifyAdditionalFieldsContested() {
            Map<String, Object> data = data();

            assertThat(data.get("DivorceCaseNumber"), is("DD98D76543"));
            assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
            assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
            assertThat(data.get("GeneralOrderCourt"),is("Nottingham County Court and Family Court"));
            assertThat(data.get("GeneralOrderJudgeDetails"), is("Her Honour Judge Contested"));
            assertThat(data.get("GeneralOrderRecitals"), is("Contested Recitals"));
            assertThat(data.get("GeneralOrderDate"), is("01/06/2020"));
            assertThat(data.get("GeneralOrderBodyText"), is("Test is dummy text for contested"));
            assertThat(data.get("GeneralOrderHeaderOne"), is("In the Family Court"));
            assertThat(data.get("GeneralOrderHeaderTwo"), is("sitting in the"));
            assertThat(data.get("GeneralOrderCourtSitting"), is("SITTING AT the Family Court at the "));

        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}
