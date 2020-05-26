package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;

public class HearingDocumentServiceTest {

    private DocumentClient generatorClient;
    private ObjectMapper mapper = new ObjectMapper();

    private GenericDocumentService genericDocumentService;
    private HearingDocumentService hearingDocumentService;

    private static final String DATE_OF_HEARING = "2019-01-01";
    private static final String FORM_C = "formC";
    private static final String FORM_G = "formG";

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setFormCFastTrackTemplate("firstTrackTemplate");
        config.setFormCNonFastTrackTemplate("nonFastfirstTrackTemplate");
        config.setFormGTemplate("formGTemplate");
        config.setFormCFileName("Form-C.pdf");
        config.setFormGFileName("Form-G.pdf");
        config.setMiniFormFileName("file_name");

        generatorClient = new TestDocumentClient();
        genericDocumentService = new GenericDocumentService(generatorClient);
        hearingDocumentService = new HearingDocumentService(genericDocumentService, config, new DocumentHelper(mapper));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        ((TestDocumentClient) generatorClient).verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormC() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN,
                makeItJudiciaryFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        ((TestDocumentClient) generatorClient).verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormG() {
        Map<String, Object> result = hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
        assertCaseDocument((CaseDocument) result.get(FORM_C));
        assertCaseDocument((CaseDocument) result.get(FORM_G));
        ((TestDocumentClient) generatorClient).verifyAdditionalNonFastTrackFields();
    }

    @Test(expected = CompletionException.class)
    public void unsuccessfulGenerateHearingDocuments() {
        ((TestDocumentClient) generatorClient).throwException();
        hearingDocumentService.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails(NO_VALUE);
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails(YES_VALUE);
    }

    private CaseDetails makeItJudiciaryFastTrackDecisionCase() {
        Map<String, Object> caseData =
                ImmutableMap.of(FAST_TRACK_DECISION, NO_VALUE,
                        CASE_ALLOCATED_TO, YES_VALUE, HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData =
                ImmutableMap.of(FAST_TRACK_DECISION, isFastTrackDecision, HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder().data(caseData).build();
    }

    private Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);
        return document;
    }

    private class TestDocumentClient implements DocumentClient {

        private Map<String, Object> value;
        private boolean throwException;

        @Override
        public Document generatePdf(DocumentGenerationRequest request, String authorizationToken) {
            if (throwException) {
                throw new RuntimeException();
            }

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
        public DocumentValidationResponse checkUploadedFileType(String fileUrl, String authorizationToken) {
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

        void throwException() {
            this.throwException = true;
        }

        void verifyAdditionalFastTrackFields() {
            Map<String, Object> data = data();
            assertThat(data.get("formCCreatedDate"), is(notNullValue()));
            assertThat(data.get("eventDatePlus21Days"), is(notNullValue()));
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }

        void verifyAdditionalNonFastTrackFields() {
            Map<String, Object> data = data();
            assertThat(data.get("formCCreatedDate"), is(notNullValue()));
            assertThat(data.get("hearingDateLess35Days"), is(notNullValue()));
            assertThat(data.get("hearingDateLess14Days"), is(notNullValue()));
        }
    }
}