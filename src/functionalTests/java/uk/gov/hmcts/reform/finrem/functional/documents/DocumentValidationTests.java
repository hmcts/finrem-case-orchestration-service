package uk.gov.hmcts.reform.finrem.functional.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Slf4j
public class DocumentValidationTests extends IntegrationTestBase {

    private static String MINIFORMA_JSON = "documentGeneratePayload1.json";
    private static String DATAPATH = "data";
    @Value("${cos.document.miniform.api}")
    private String generatorUrl;
    private String consentedDir = "/json/consented/";
    private ObjectMapper objectMapper = new ObjectMapper();
    private CallbackRequest callbackRequest;
    @Value("${cos.consentOrder.document.validation.api}")
    private String fileUploadCheckUrl;

    private void setUpCaseDetails(String fileName) throws Exception {
        try (InputStream resourceAsStream =
                     getClass().getResourceAsStream(consentedDir + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void verifyDocumentValidationShouldReturnOkResponseCode() throws Exception {
        setUpCaseDetails(MINIFORMA_JSON);
        // generate pdf document
        JsonPath jsonPathEvaluator = generateDocument(MINIFORMA_JSON, generatorUrl, consentedDir);
        log.info("response is :" + jsonPathEvaluator.prettyPrint());
        Object object = jsonPathEvaluator.get("data.miniFormA");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseDocument caseDocument = convertToCaseDocument(object);
        log.info("case document " + caseDocument.getDocumentBinaryUrl());
        data.put("consentOrder", caseDocument);
        caseDetails.setData(data);
        callbackRequest.setCaseDetails(caseDetails);
        // call fileupload endpoint and assert
        JsonPath responseData = utils.getResponseData(fileUploadCheckUrl, callbackRequest, DATAPATH);
        System.out.println(responseData);

    }

    private io.restassured.path.json.JsonPath generateDocument(String jsonFileName, String url, String journeyType) {

        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).andReturn();

        int statusCode = jsonResponse.getStatusCode();
        assertEquals(statusCode, 200);

        return jsonResponse.jsonPath();
    }

    private CaseDocument convertToCaseDocument(Object object) {
        objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, CaseDocument.class);
    }
}
