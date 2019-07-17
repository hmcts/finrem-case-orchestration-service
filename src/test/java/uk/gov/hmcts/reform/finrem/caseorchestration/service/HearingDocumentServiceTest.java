package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;

import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;

public class HearingDocumentServiceTest {

    private DocumentClient generatorClient;
    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private HearingDocumentService service;

    private static final String DATE_OF_HEARING = "2019-01-01";

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setFormCFastTrackTemplate("firstTrackTemplate");
        config.setFormCNonFastTrackTemplate("nonFastfirstTrackTemplate");
        config.setFormGTemplate("formGTemplate");
        config.setFormCFileName("Form-C.pdf");
        config.setFormGFileName("Form-G.pdf");
        config.setMiniFormFileName("file_name");

        generatorClient = new TestDocumentClient();
        service = new HearingDocumentService(generatorClient, config, mapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();
        service.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormC() {
        Map<String, Object> result = service.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());
        doCaseDocumentAssert((CaseDocument) result.get("formC"));
        ((TestDocumentClient) generatorClient).verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateJudiciaryBasedFastTrackFormC() {
        Map<String, Object> result = service.generateHearingDocuments(AUTH_TOKEN,
                makeItJudiciaryFastTrackDecisionCase());
        doCaseDocumentAssert((CaseDocument) result.get("formC"));
        ((TestDocumentClient) generatorClient).verifyAdditionalFastTrackFields();
    }

    @Test
    public void generateNonFastTrackFormCAndFormG() {
        Map<String, Object> result = service.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
        doCaseDocumentAssert((CaseDocument) result.get("formC"));
        doCaseDocumentAssert((CaseDocument) result.get("formG"));
        ((TestDocumentClient) generatorClient).verifyAdditionalNonFastTrackFields();
    }

    @Test(expected = CompletionException.class)
    public void unsuccessfulGenerateHearingDocuments() {
        ((TestDocumentClient) generatorClient).throwException();
        service.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails("No");
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails("Yes");
    }

    private CaseDetails makeItJudiciaryFastTrackDecisionCase() {
        Map<String, Object> caseData =
                ImmutableMap.of("fastTrackDecision", "No",
                        CASE_ALLOCATED_TO, "Yes", HEARING_DATE, DATE_OF_HEARING);
        return CaseDetails.builder().data(caseData).build();
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData =
                ImmutableMap.of("fastTrackDecision", isFastTrackDecision, HEARING_DATE, DATE_OF_HEARING);
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
        public Document generatePDF(DocumentRequest request, String authorizationToken) {
            if (throwException) {
                throw new RuntimeException();
            }

            this.value = request.getValues();
            return document();
        }

        @Override
        public void bulkPrint(BulkPrintRequest bulkPrintRequest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DocumentValidationResponse checkUploadedFileType(String fileUrl,
                                                                String authorizationToken) {
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