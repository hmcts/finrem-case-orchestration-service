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

    @Value("${cos.document.miniform.api}")
    private String generatorUrl;

    @Value("${document.management.store.baseUrl}")
    private String dmStoreBaseUrl;

    //@Test
    public void verifyDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess("documentGeneratePayload.json");
    }

    //@Test
    public void verifyDocumentGenerationPostResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();
        assertTrue(jsonPathEvaluator.get("data.miniFormA.document_filename").toString()
                .equalsIgnoreCase("MiniFormA.pdf"));

        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("applicationIssued"));

    }

    //@Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();

        System.out.println("Validate Post Payment data:" + response.jsonPath().prettyPrint());
        String url = jsonPathEvaluator.get("data.miniFormA.document_url");

        System.out.println("AccessingGeneratedDocument url :" + url);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(url));

        Response response1 = accessGeneratedDocument(fileRetrieveUrl(url));
        JsonPath jsonPathEvaluator1 = response1.jsonPath();
        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("MiniFormA.pdf"));
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }


    //@Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {
        Response response = generateDocument("documentGeneratePayload.json");
        JsonPath jsonPathEvaluator = response.jsonPath();

        String documentUrl = jsonPathEvaluator.get("data.miniFormA.document_binary_url");

        System.out.println("AccessingGeneratedDocument binaryUrl :" + documentUrl);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    private void validatePostSuccess(String jsonFileName) {
        System.out.println("GenerateDocUrl :" + generatorUrl);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(generatorUrl)
                .then()
                .assertThat().statusCode(200);
    }


    private Response generateDocument(String jsonFileName) {
        System.out.println("GenerateDocUrl :" + generatorUrl);

        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(generatorUrl).andReturn();
        return jsonResponse;
    }

    private void validatePostSuccessForaccessingGeneratedDocument(String url) {
        System.out.println("AccessingGeneratedDocument :" + url);

        System.out.println("AccessingGeneratedDocument :" + url);
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

    private String fileRetrieveUrl(String url) {
        if (url != null && url.contains("dm-store:8080")) {
            return url.replace("http://dm-store:8080", dmStoreBaseUrl);
        }

        return url;
    }

}




