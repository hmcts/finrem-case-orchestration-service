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
    private static String MINIFORMA_JSON = "documentGeneratePayload1.json";
    private static String GENERALORDER_JSON = "document-rejected-order1.json";
    private String url1;
    private JsonPath jsonPathEvaluator;
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
        validatePostSuccess(MINIFORMA_JSON,generatorUrl);
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(GENERALORDER_JSON,documentRejectedOrderUrl);
    }

    @Test
    public void verifyDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(MINIFORMA_JSON,generatorUrl);

        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("applicationIssued"));
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(GENERALORDER_JSON,documentRejectedOrderUrl);

        assertTrue(jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentType").toString()
                .equalsIgnoreCase("generalOrder"));
    }

    @Test
    public void verifyRejectedOrderGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERALORDER_JSON,documentRejectedOrderUrl,
                "document","generalOrder");

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("GeneralOrder.pdf"));

    }

    @Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_JSON,generatorUrl,"document","miniForma");

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }


    @Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_JSON,generatorUrl,"binary","miniForma");

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    @Test
    public void downloadRejectOrderDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERALORDER_JSON,documentRejectedOrderUrl,
                "binary","generalOrder");
        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains("Approved by:  District Judge test3"));
    }


    private void validatePostSuccess(String jsonFileName,String url) {

        System.out.println("url " + url);
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }


    private JsonPath generateDocument(String jsonFileName, String url) {

        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url).andReturn();
        return jsonResponse.jsonPath();
    }


    private String getDocumentUrlOrDocumentBinaryUrl(String jsonFile,String url ,String urlType,String documentType) {

        switch (documentType) {
            case "miniForma":
                jsonPathEvaluator = generateDocument(jsonFile, url);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_url");
                } else if (urlType.equals("binary")) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_binary_url");
                }
                break;
            case "generalOrder":
                jsonPathEvaluator = generateDocument(jsonFile, url);
                if (urlType.equals("document")) {

                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_url");
                } else if (urlType.equals("binary"))  {
                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_binary_url");
                }
                break;
            default :
                jsonPathEvaluator = generateDocument(jsonFile, url);
                break;
        }

        return url1;

    }

    private void validatePostSuccessForaccessingGeneratedDocument(String url) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeadersWithUserId())
                .when().get(url)
                .then().assertThat().statusCode(200);

    }

    private JsonPath accessGeneratedDocument(String url) {
        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeadersWithUserId())
                .when().get(url)
                .andReturn();
        return jsonResponse.jsonPath();
    }

    private String fileRetrieveUrl(String url) {
        if (url != null && url.contains("dm-store:8080")) {
            return url.replace("http://dm-store:8080", dmStoreBaseUrl);
        }

        return url;
    }

}




