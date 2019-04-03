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

    @Value("${cos.document.miniform.api}")
    private String generatorUrl;

    @Value("${document.management.store.baseUrl}")
    private String dmStoreBaseUrl;

    @Value("${document.rejected.order}")
    private String documentRejectedOrderUrl;

    @Test
    public void verifyDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess("documentGeneratePayload.json",generatorUrl);
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess("document-rejected-order.json",documentRejectedOrderUrl);
    }


    @Test
    public void verifyDocumentGenerationPostResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json",generatorUrl);
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println("Response Body" + response.getBody().prettyPrint());

        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("applicationIssued"));
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationPostResponseContent() {
        Response response = generateDocument("document-rejected-order.json",documentRejectedOrderUrl);
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println("Response Body" + response.getBody().prettyPrint());

        assertTrue(jsonPathEvaluator.get("data.UploadOrder.DocumentType").toString()
                .equalsIgnoreCase("generalOrder"));
    }

    @Test
    public void verifyRejectedOrderGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {
        Response response = generateDocument("document-rejected-order.json",documentRejectedOrderUrl);
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println("Response Body" + response.getBody().prettyPrint());

        String url = jsonPathEvaluator.get("uploadOrder.DocumentLink.document_url");

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(url));

        Response response1 = accessGeneratedDocument(fileRetrieveUrl(url));
        JsonPath jsonPathEvaluator1 = response1.jsonPath();
        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("GeneralOrder.pdf"));

    }

    @Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {
        Response response = generateDocument("documentGeneratePayload.json",generatorUrl);
        JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println("Response Body" + response.getBody().prettyPrint());

        String url = jsonPathEvaluator.get("data.miniFormA.document_url");

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(url));

        Response response1 = accessGeneratedDocument(fileRetrieveUrl(url));
        JsonPath jsonPathEvaluator1 = response1.jsonPath();
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }


    @Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {
        Response response = generateDocument("documentGeneratePayload.json",generatorUrl);

        JsonPath jsonPathEvaluator = response.jsonPath();

        String documentUrl = jsonPathEvaluator.get("data.miniFormA.document_binary_url");

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    private void validatePostSuccess(String jsonFileName,String url) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }


    private Response generateDocument(String jsonFileName, String url) {

        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url).andReturn();
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

    private String fileRetrieveUrl(String url) {
        if (url != null && url.contains("dm-store:8080")) {
            return url.replace("http://dm-store:8080", dmStoreBaseUrl);
        }

        return url;
    }

}




