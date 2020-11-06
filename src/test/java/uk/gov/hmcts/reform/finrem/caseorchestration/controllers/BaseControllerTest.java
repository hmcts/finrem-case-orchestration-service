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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseControllerTest extends BaseTest {

    @Autowired
    protected WebApplicationContext applicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mvc;
    protected JsonNode requestContent;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    void doEmptyCaseDataSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/empty-casedata.json").toURI()));
    }

    void doValidCaseDataSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));
    }

    void doValidConsentOrderApprovedSetup() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/consent-in-contested-application-approved.json").toURI()));
    }

    void doValidCaseDataSetUpForPaperApplication() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/bulkprint/bulk-print-paper-application.json").toURI()));
    }

    void doValidCaseDataSetUpNoPensionCollection() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/bulkprint/bulk-print-no-pension-collection.json").toURI()));
    }

    void doMissingLatestConsentOrder() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/hwf.json").toURI()));
    }

    void doValidConsentInContestWithPensionData() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/consent-in-contested-with-pension.json").toURI()));
    }

    void doValidRefusalOrder() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/refusal-order-contested.json").toURI()));
    }

    void doValidCaseDataSetUpForAdditionalHearing() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/bulkprint/bulk-print-additional-hearing.json").toURI()));
    }

    protected CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(123)).data(caseData).build();
        return CallbackRequest.builder().caseDetails(caseDetails).build();
    }

    CaseDocument getCaseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentUrl("http://doc1");
        caseDocument.setDocumentBinaryUrl("http://doc1/binary");
        caseDocument.setDocumentFilename("doc1");
        return caseDocument;
    }

    protected String resourceContentAsString(String resourcePath) {
        try {
            return objectMapper.readTree(new File(getClass().getResource(resourcePath).toURI())).toString();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
