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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedRefusalOrderData;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;

public class ContestedDraftOrderNotApprovedServiceTest {

 private DocumentClient generatorClient;
    private ObjectMapper mapper = new ObjectMapper();
    private GenericDocumentService genericDocumentService;
    private ContestedDraftOrderNotApprovedService refusalOrderService;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setGeneralOrderTemplate("test_template");
        config.setGeneralOrderFileName("test_file");

        generatorClient = new ContestedDraftOrderNotApprovedServiceTest.TestDocumentClient();
        genericDocumentService = new GenericDocumentService(generatorClient);
        refusalOrderService = new ContestedDraftOrderNotApprovedService(genericDocumentService, new DocumentHelper(mapper), config, mapper);
    }

    @Test
    public void generateRefusalOrder() throws Exception {
        Map<String, Object> documentMap = refusalOrderService.createRefusalOrder(AUTH_TOKEN, contestedCaseDetails());

        CaseDocument result = (CaseDocument) documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT);
        doCaseDocumentAssert(result);
        ((ContestedDraftOrderNotApprovedServiceTest.TestDocumentClient) generatorClient).verifyAdditionalFieldsContested();
    }

    @Test
    public void submitContestedGeneralOrder() throws Exception {
        Map<String, Object> documentMap = refusalOrderService.populateRefusalOrderCollection(contestedCaseDetails());

        List<ContestedRefusalOrderData> refusalOrders =
            (List<ContestedRefusalOrderData>)documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_COLLECTION);

        assertThat(refusalOrders, hasSize(2));
        assertThat(refusalOrders.get(0).getId(), is("1234"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getNotApprovedDocument().getDocumentUrl(), is("http://dm-store/lhjbyuivu87y989hijbb"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getNotApprovedDocument().getDocumentFilename(),
            is("app_docs.pdf"));
        assertThat(refusalOrders.get(0).getContestedRefusalOrder().getNotApprovedDocument().getDocumentBinaryUrl(),
            is("http://dm-store/lhjbyuivu87y989hijbb/binary"));

        assertThat(refusalOrders.get(1).getId(), notNullValue());
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getNotApprovedDocument().getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getNotApprovedDocument().getDocumentFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(refusalOrders.get(1).getContestedRefusalOrder().getNotApprovedDocument().getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));

        CaseDocument latestRefusalOrder = (CaseDocument)documentMap.get(CONTESTED_APPLICATION_NOT_APPROVED_LATEST_DOCUMENT);
        assertThat(latestRefusalOrder.getDocumentUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d"));
        assertThat(latestRefusalOrder.getDocumentFilename(),
            is("refusalOrderTestFilename.pdf"));
        assertThat(latestRefusalOrder.getDocumentBinaryUrl(),
            is("http://document-management-store:8080/documents/015500ba-c524-4614-86e5-c569f82c718d/binary"));
    }

    private CaseDetails contestedCaseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/refusal-order-contested.json")) {
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

        void verifyAdditionalFieldsContested() {
            Map<String, Object> data = data();

            assertThat(data.get("ApplicantName"), is("Contested Applicant Name"));
            assertThat(data.get("RespondentName"), is("Contested Respondent Name"));
            assertThat(data.get("Court"),is("Nottingham County Court and Family Court"));
            assertThat(data.get("JudgeDetails"), is("Her Honour Judge Contested"));
            assertThat(data.get("ContestOrderNotApprovedRefusalReasonsFormatted"),
                is("- Test Reason 1+ \n- Test Reason 2"));
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}