package uk.gov.hmcts.reform.finrem.functional.documents;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertNull;
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
    private static String MINIFORMA_CONTESTED_JSON = "generate-contested-form-A1.json";
    private static String CONTESTED_FORMC_JSON = "validate-hearing-with-fastTrackDecision1.json";
    private static String APPLICANT_NAME_HEARING = "Guy";
    private static String DIVORCE_CASENO_HEARING = "DD12D12345";
    private static String SOLICITOR_REF_HEARING = "LL01";
    private static String DOCUMENT_FORMC = "Form C";
    private static String DOCUMENT_FORMG = "Form G";
    private static String CONTESTED_FORMG_JSON = "validate-hearing-withoutfastTrackDecision1.json";
    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";


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


    @Value("${cos.document.hearing.api}")
    private String generatorHearingUrl;


    @Value("${cos.document.contested.miniform.api}")
    private String generateContestedUrl;


    @Value("${cos.document.contested.draft.api}")
    private String generateContestedDraftUrl;



    @Value("${cos.document.hearing.api}")
    private String generateHearingUrl;

    @Test
    public void verifyDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(MINIFORMA_JSON, generatorUrl, consentedDir);
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(GENERALORDER_JSON, documentRejectedOrderUrl, consentedDir);
    }

    @Test
    public void verifyContestedDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(MINIFORMA_CONTESTED_JSON, generateContestedDraftUrl, contestedDir);
    }

    @Test
    public void verifyContestedDraftDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(CONTESTED_FORMC_JSON, generateContestedUrl, contestedDir);
    }

    @Test
    public void verifyContestedFormCDocumentGenerationShouldReturnOkResponseCode() {
        validatePostSuccess(CONTESTED_FORMC_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyContestedFormGDocumentGenerationShouldReturnOkResponseCode() {
        System.out.println("status code is :" + getStatusCode(CONTESTED_FORMG_JSON, generateHearingUrl, contestedDir));
        validatePostSuccess(CONTESTED_FORMG_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(MINIFORMA_JSON, generatorUrl, consentedDir);

        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("applicationIssued"));
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(GENERALORDER_JSON, documentRejectedOrderUrl, consentedDir);

        assertTrue(jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentType").toString()
                .equalsIgnoreCase("generalOrder"));
    }

    @Test
    public void verifyContestedDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(MINIFORMA_CONTESTED_JSON,
                generateContestedUrl, contestedDir);

        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("applicationIssued"));
    }

    @Test
    public void verifyContestedDraftDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(MINIFORMA_CONTESTED_JSON,
                generateContestedDraftUrl, contestedDir);
        assertNull(jsonPathEvaluator.get("data.state."));
    }


    @Test
    public void verifyContestedFormCDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(CONTESTED_FORMC_JSON, generateHearingUrl, contestedDir);
        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("prepareForHearing"));
    }

    @Test
    public void verifyContestedFormGDocumentGenerationPostResponseContent() {

        System.out.println("status code is :" + getStatusCode(CONTESTED_FORMG_JSON, generateHearingUrl, contestedDir));

        JsonPath jsonPathEvaluator = generateDocument(CONTESTED_FORMG_JSON, generateHearingUrl, contestedDir);
        System.out.println("FormG document generation : " + jsonPathEvaluator.prettyPrint());
        assertTrue(jsonPathEvaluator.get("data.state.").toString()
                .equalsIgnoreCase("prepareForHearing"));
    }

    @Test
    public void verifyRejectedOrderGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERALORDER_JSON, documentRejectedOrderUrl,
                "document", "generalOrder", consentedDir);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("GeneralOrder.pdf"));

    }

    @Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_JSON, generatorUrl,
                "document", "miniForma", consentedDir);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_CONTESTED_JSON, generateContestedUrl,
                "document", "miniForma", contestedDir);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedFormCContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORMC_JSON, generateHearingUrl,
                "document", "hearing", contestedDir);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedFormGContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORMG_JSON, generateHearingUrl,
                "document", "hearing", contestedDir);

        validatePostSuccessForaccessingGeneratedDocument(fileRetrieveUrl(documentUrl));

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_JSON, generatorUrl,
                "binary", "miniForma", consentedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    @Test
    public void downloadRejectOrderDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERALORDER_JSON, documentRejectedOrderUrl,
                "binary", "generalOrder", consentedDir);
        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains("Approved by:  District Judge test3"));
    }

    @Test
    public void downloadContestedDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINIFORMA_CONTESTED_JSON, generateContestedUrl,
                "binary", "miniForma", contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        System.out.println("Document Content : " + documentContent.toString());

        //assertTrue(documentContent.contains(SOLICITOR_FIRM));
        //assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        //assertTrue(documentContent.contains(SOLICITOR_REF));

    }

    @Test
    public void downloadContestedFormCDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORMC_JSON, generateHearingUrl,
                "binary", "hearing", contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        System.out.println("Document Content : " + documentContent.toString());

        assertTrue(documentContent.contains(APPLICANT_NAME_HEARING));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF_HEARING));

    }

    @Test
    public void downloadContestedFormGDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORMG_JSON, generateHearingUrl,
                "binary", "hearing", contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        System.out.println("Document Content : " + documentContent.toString());
        assertTrue(documentContent.contains(APPLICANT_NAME_HEARING));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF_HEARING));

    }


    private void validatePostSuccess(String jsonFileName, String url, String journeyType) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }


    private JsonPath generateDocument(String jsonFileName, String url, String journeyType) {

        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).andReturn();
        return jsonResponse.jsonPath();
    }

    private int getStatusCode(String jsonFileName, String url, String journeyType) {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).getStatusCode();
    }

    private String getDocumentUrlOrDocumentBinaryUrl(String jsonFile, String url, String urlType,
                                                     String documentType, String journeyType) {

        switch (documentType) {
            case "miniForma":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_url");
                } else if (urlType.equals("binary")) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_binary_url");
                }
                break;
            case "generalOrder":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                System.out.println("status code is :" + getStatusCode(jsonFile, url, journeyType));
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_url");
                } else if (urlType.equals("binary")) {
                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_binary_url");
                }
                break;
            case "hearing":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                System.out.println("status code is :" + getStatusCode(jsonFile, url, journeyType));
                System.out.println("generate Document reponse : " + jsonPathEvaluator.prettyPrint());
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.formC.document_url");
                } else if (urlType.equals("binary")) {
                    url1 = jsonPathEvaluator.get("data.formC.document_binary_url");
                }
                break;
            case "hearingG":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                System.out.println("generate Document reponse : " + jsonPathEvaluator.prettyPrint());
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.formG.document_url");
                } else if (urlType.equals("binary")) {
                    url1 = jsonPathEvaluator.get("data.formG.document_binary_url");
                }
                break;
            default:
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
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

