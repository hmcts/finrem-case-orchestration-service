package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER;

public class GeneralLetterServiceTest {

    private DocumentClient generatorClient;
    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private GeneralLetterService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setGeneralLetterTemplate("test_template");
        config.setGeneralLetterFileName("test_file");

        generatorClient = new TestDocumentClient();
        service = new GeneralLetterService(generatorClient, config, mapper);
    }

    @Test
    public void generateGeneralLetter() throws Exception {
        Map<String, Object> documentMap = service.createGeneralLetter(AUTH_TOKEN, caseDetails());

        List<GeneralLetterData> result = (List<GeneralLetterData>) documentMap.get(GENERAL_LETTER);
        assertThat(result, hasSize(2));

        doCaseDocumentAssert(result.get(0).getGeneralLetter().getGeneratedLetter());
        doCaseDocumentAssert(result.get(1).getGeneralLetter().getGeneratedLetter());
        ((TestDocumentClient)generatorClient).verifyAdditionalFields();
    }

    private CaseDetails caseDetails() throws Exception {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/general-letter.json")) {
            return mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(DOC_URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }

    private class TestDocumentClient implements DocumentClient {

        private Map<String, Object> value;

        @Override
        public Document generatePDF(DocumentGenerationRequest request, String authorizationToken) {
            this.value = request.getValues();
            return document();
        }

        @Override
        public void deleteDocument(String fileUrl, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void generateApprovedConsentOrder(CallbackRequest callback, String authorizationToken) {
            throw new UnsupportedOperationException();
        }

        void verifyAdditionalFields() {
            Map<String, Object> data = data();
            assertThat(data.get("generalLetterCreatedDate"), is(notNullValue()));
            assertThat(data.get("ccdCaseNumber"), is(1234567890L));
        }

        private Map<String, Object> data() {
            CaseDetails caseDetails = (CaseDetails) value.get("caseDetails");
            return caseDetails.getData();
        }
    }
}