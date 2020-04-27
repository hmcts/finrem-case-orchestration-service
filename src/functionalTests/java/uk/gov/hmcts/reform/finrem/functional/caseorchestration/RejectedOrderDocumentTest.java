package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_REFUSAL_PREVIEW_COLLECTION;

@RunWith(SerenityRunner.class)
public class RejectedOrderDocumentTest extends IntegrationTestBase {

    @Autowired
    private FunctionalTestUtils functionalTestUtils;

    private ObjectMapper objectMapper = new ObjectMapper();
    private CallbackRequest callbackRequest = null;

    private final String contestedDir = "/json/contested/";
    private final String consentedDir = "/json/consented/";

    @Value("${cos.preview.consentOrder.not.approved}")
    private String previewConsentOrderNotApprovedEndPoint;

    @Value("${cos.consentOrder.not.approved}")
    private String consentOrderNotApprovedEndPoint;

    @Test
    public void verifyPreviewConsentOrderNotApproved() {

        InputStream resourceAsStream =  getClass().getResourceAsStream(consentedDir + "approved-consent-order.json");
        DocumentContext documentContext = JsonPath.parse(resourceAsStream);

        HashMap<String,Object> caseDetails = documentContext.read("$.case_details.case_data");
        caseDetails.remove(ORDER_REFUSAL_PREVIEW_COLLECTION);

        try {
            callbackRequest = objectMapper.readValue(documentContext.jsonString(), CallbackRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("Error generating CallbackRequest from approved-consent-order.json ");
        }

        Response response = functionalTestUtils.getResponseData(previewConsentOrderNotApprovedEndPoint,callbackRequest);

        List<Object> orderRefusalPreviewDocuments = JsonPath.parse(response.asString())
            .read("$.data[?(@.orderRefusalPreviewDocument)]");
        assertEquals("Request failed " + response.getStatusCode(), 200, response.getStatusCode());
        assertTrue("Order Refusal Preview Document not found ", orderRefusalPreviewDocuments.size() > 0);
    }

    @Test
    public void verifyConsentOrderNotApproved() {

        InputStream resourceAsStream =  getClass().getResourceAsStream(consentedDir + "approved-consent-order.json");
        DocumentContext documentContext = JsonPath.parse(resourceAsStream);
        int uploadOrdersBeforeCount = ((List)documentContext.read("$.case_details.case_data.uploadOrder")).size();

        try {
            callbackRequest = objectMapper.readValue(documentContext.jsonString(), CallbackRequest.class);
        } catch (IOException e) {
            throw new RuntimeException("Error generating CallbackRequest from approved-consent-order.json ");
        }

        Response response = functionalTestUtils.getResponseData(consentOrderNotApprovedEndPoint,callbackRequest);

        List<Object> uploadOrders = JsonPath.parse(response.getBody().asString())
            .read("$.data.uploadOrder[?(@.id)].id");
        assertEquals("Request failed " + response.getStatusCode(), 200, response.getStatusCode());
        assertEquals("expected uploadOrder size not matching  ", uploadOrdersBeforeCount + 1, uploadOrders.size());
    }
}
