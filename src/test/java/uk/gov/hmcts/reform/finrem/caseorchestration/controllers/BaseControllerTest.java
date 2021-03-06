package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseControllerTest extends BaseTest {

    @Autowired protected WebApplicationContext applicationContext;
    @Autowired protected ObjectMapper objectMapper;

    protected MockMvc mvc;
    protected JsonNode requestContent;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    protected void doEmptyCaseDataSetUp() {
        loadRequestContentWith("/fixtures/empty-casedata.json");
    }

    protected void doValidCaseDataSetUp() {
        loadRequestContentWith("/fixtures/pba-validate.json");
    }

    protected void doValidConsentOrderApprovedSetup() {
        loadRequestContentWith("/fixtures/contested/consent-in-contested-application-approved.json");
    }

    protected void doValidCaseDataSetUpForPaperApplication() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-paper-application.json");
    }

    protected void doValidCaseDataSetUpNoPensionCollection() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-no-pension-collection.json");
    }

    protected void doMissingLatestConsentOrder() {
        loadRequestContentWith("/fixtures/hwf.json");
    }

    protected void doValidConsentInContestWithPensionData() {
        loadRequestContentWith("/fixtures/contested/consent-in-contested-with-pension.json");
    }

    protected void doValidRefusalOrder() {
        loadRequestContentWith("/fixtures/refusal-order-contested.json");
    }

    protected void doValidCaseDataSetUpForAdditionalHearing() {
        loadRequestContentWith("/fixtures/bulkprint/bulk-print-additional-hearing.json");
    }

    protected CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).data(caseData).build();
        return CallbackRequest.builder().caseDetails(caseDetails).build();
    }

    protected CaseDocument getCaseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl("http://doc1");
        caseDocument.setDocumentBinaryUrl("http://doc1/binary");
        caseDocument.setDocumentFilename("doc1");
        return caseDocument;
    }

    protected String resourceContentAsString(String resourcePath) {
        return readJsonNodeFromFile(resourcePath).toString();
    }

    protected void loadRequestContentWith(String jsonPath) {
        requestContent = readJsonNodeFromFile(jsonPath);
    }

    private JsonNode readJsonNodeFromFile(String jsonPath) {
        try {
            return objectMapper.readTree(
                new File(getClass()
                    .getResource(jsonPath)
                    .toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
