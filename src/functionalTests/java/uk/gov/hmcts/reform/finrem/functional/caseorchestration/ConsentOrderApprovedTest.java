package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;
import uk.gov.hmcts.reform.finrem.functional.util.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SerenityRunner.class)
public class ConsentOrderApprovedTest extends IntegrationTestBase {

    @Autowired
    private FunctionalTestUtils functionalTestUtils;

    @Autowired
    private ServiceUtils serviceUtils;

    @Value("${cos.consentOrder.approved}")
    private String consentOrderApprovedUrl;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void verifyConsentOrderApprovedForConsentedCaseForApplicant() {
        CallbackRequest callbackRequest = null;
        InputStream resourceAsStream;
        resourceAsStream = getClass().getResourceAsStream("/json/consented/approved-consent-order.json");

        Map<String,String> uploadedDoc = null;
        try {
            uploadedDoc = serviceUtils.uploadFileToEmStore("fileTypes/sample.pdf","application/pdf");
        } catch (JSONException e) {
            throw new RuntimeException("Exception uploading file to evidence store ",e);
        }

        DocumentContext documentContext = JsonPath.parse(resourceAsStream).set("$..latestConsentOrder",uploadedDoc);
        documentContext.set("$..pensionCollection[0].value.uploadedDocument",uploadedDoc);
        try {
            callbackRequest = objectMapper.readValue(documentContext.jsonString(), CallbackRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response response = functionalTestUtils.getResponseData(consentOrderApprovedUrl,callbackRequest);
        assertEquals("Request failed " + response.getStatusCode(), 200, response.getStatusCode());
    }

    @Ignore
    @Test
    public void verifyConsentOrderApprovedForConsentedCaseWhenApplicantRepresentedBySolicitor() {

    }
}
