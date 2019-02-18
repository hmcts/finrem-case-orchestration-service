package uk.gov.hmcts.reform.finrem.functional.documents;


import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertTrue;


@RunWith(SerenityRunner.class)

public class FinancialRemedyDocumentGeneratorTests extends IntegrationTestBase {

    private static String SOLICITOR_FIRM = "Michael Jones & Partners";
    private static String SOLICITOR_NAME = "Jane Smith";
    private static String APPLICANT_NAME = "Williams";
    private static String DIVORCE_CASENO = "DD12D12345";
    private static String SOLICITOR_REF = "JAW052018";
    @Value("${idam.s2s-auth.microservice}")
    private String microservice;

    @Value("${idam.oauth2.client.secret}")
    private String authClientSecret;


    @Test
    public void verifyDocumentGenerationShouldReturnOkResponseCode() {

        validatePostSuccess("documentGeneratePayload.json");
    }

    @Test
    public void verifyDocumentGenerationPostResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();
        assertTrue(jsonPathEvaluator.get("fileName").toString().equalsIgnoreCase("MiniFormA.pdf"));
        assertTrue(jsonPathEvaluator.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
    }

    @Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String url = jsonPathEvaluator.get("url");
        validatePostSuccessForaccessingGeneratedDocument(url);
        validatePostSuccessForaccessingGeneratedDocument(url);
        Response response1 = accessGeneratedDocument(url);
        JsonPath jsonPathEvaluator1 = response1.jsonPath();
        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("MiniFormA.pdf"));
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }


    @Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();
        String documentUrl = jsonPathEvaluator.get("url") + "/binary";
        String documentContent = utils.downloadPdfAndParseToString(documentUrl);
        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    private void validatePostSuccess(String jsonFileName) {
        IntegrationTestBase.setDocumentGeneratorServiceUrlAsBaseUri();
        SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName))
            .when().post()
            .then()
            .assertThat().statusCode(200);
    }


    private Response generateDocument(String jsonFileName) {
        IntegrationTestBase.setDocumentGeneratorServiceUrlAsBaseUri();
        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName))
            .when().post().andReturn();
        return jsonResponse;
    }

    private void validatePostSuccessForaccessingGeneratedDocument(String url) {

        SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeadersWithUserId())
            .when().get(url)
            .then().assertThat().statusCode(200);

    }

    private Response accessGeneratedDocument(String url) {
        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeadersWithUserId())
            .when().get(url)
            .andReturn();
        return jsonResponse;
    }

}




