package uk.gov.hmcts.reform.finrem.functional.documents;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BINARY_URL_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@RunWith(SerenityRunner.class)
public class FinancialRemedyDocumentGeneratorTests extends IntegrationTestBase {

    private static String APPLICANT_NAME = "Williams";
    private static String DIVORCE_CASENO = "DD12D12345";
    private static String SOLICITOR_FIRM = "Michael Jones & Partners";
    private static String SOLICITOR_NAME = "Jane Smith";
    private static String SOLICITOR_REF = "JAW052018";
    private static String GENERAL_ORDER_JSON = "document-rejected-order1.json";
    private static String CONTESTED_FORM_G_JSON = "validate-hearing-without-fastTrackDecision1.json";
    private static String MINI_FORM_A_JSON = "documentGeneratePayload1.json";
    private static String MINI_FORM_A_CONTESTED_JSON = "generate-contested-form-A1.json";
    private static String CONTESTED_FORM_C_JSON = "validate-hearing-with-fastTrackDecision1.json";
    private static String APPLICANT_NAME_HEARING = "Guy";
    private static String SOLICITOR_REF_HEARING = "LL01";
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

    @Value("${document.approved.order}")
    private String documentApprovedOrderUrl;

    @Value("${case.orchestration.api}")
    private String caseOrchestration;

    @Ignore
    @Test
    public void verifyBulkPrintDocumentGenerationShouldReturnOkResponseCode() {
        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERAL_ORDER_JSON, documentRejectedOrderUrl,
            "document", "generalOrder", consentedDir);

        String payload = utils.getJsonFromFile("bulk-print.json", consentedDir)
            .replace("$DOCUMENT-BINARY-URL", documentUrl);
        jsonPathEvaluator = utils.getResponseData(caseOrchestration + "/bulk-print", payload, "data");

        if (jsonPathEvaluator.get("bulkPrintLetterIdRes") == null) {
            Assert.fail("bulk Printing not successful");
        }
    }

    @Test
    public void verifyContestedDraftDocumentGenerationShouldReturnOkResponseCode() {
        utils.validatePostSuccess(generateContestedUrl, CONTESTED_FORM_C_JSON, contestedDir);
    }

    @Test
    public void verifyDocumentGenerationPostResponseContent() {
        generateDocument(MINI_FORM_A_JSON, generatorUrl, consentedDir);
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument(GENERAL_ORDER_JSON, documentRejectedOrderUrl, consentedDir);

        assertTrue(jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentType").toString()
            .equalsIgnoreCase("generalOrder"));
    }

    @Test
    public void verifyContestedDocumentGenerationPostResponseContent() {

        generateDocument(MINI_FORM_A_CONTESTED_JSON, generateContestedUrl, contestedDir);
    }

    @Test
    public void verifyContestedDraftDocumentGenerationPostResponseContent() {

        generateDocument(MINI_FORM_A_CONTESTED_JSON, generateContestedDraftUrl, contestedDir);
    }

    @Test
    public void verifyContestedFormCDocumentGenerationPostResponseContent() {

        generateDocument(CONTESTED_FORM_C_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyContestedFormGDocumentGenerationPostResponseContent() {

        generateDocument(CONTESTED_FORM_G_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyRejectedOrderGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERAL_ORDER_JSON, documentRejectedOrderUrl,
            "document", "generalOrder", consentedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().equalsIgnoreCase("GeneralOrder.pdf"));
    }

    @Test
    public void verifyGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_JSON, generatorUrl,
            "document", MINI_FORM_A, consentedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));
        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_CONTESTED_JSON, generateContestedUrl,
            "document", MINI_FORM_A, contestedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedFormCContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORM_C_JSON, generateHearingUrl,
            "document", "hearing", contestedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void verifyGeneratedFormGContestedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORM_G_JSON, generateHearingUrl,
            "document", "hearing", contestedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("mimeType").toString().equalsIgnoreCase("application/pdf"));
        assertTrue(jsonPathEvaluator1.get("classification").toString().equalsIgnoreCase("RESTRICTED"));
    }

    @Test
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_JSON, generatorUrl,
            BINARY_URL_TYPE, MINI_FORM_A, consentedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF));
    }

    @Test
    public void downloadRejectOrderDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERAL_ORDER_JSON, documentRejectedOrderUrl,
            BINARY_URL_TYPE, "generalOrder", consentedDir);
        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertTrue(documentContent.contains("Approved by:  District Judge test3"));
    }

    @Test
    public void downloadContestedDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_CONTESTED_JSON, generateContestedUrl,
            BINARY_URL_TYPE, MINI_FORM_A, contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
    }

    @Test
    public void downloadContestedFormCDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORM_C_JSON, generateHearingUrl,
            BINARY_URL_TYPE, "hearing", contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(APPLICANT_NAME_HEARING));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF_HEARING));
    }

    @Test
    public void downloadContestedFormGDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(CONTESTED_FORM_G_JSON, generateHearingUrl,
            BINARY_URL_TYPE, "hearing", contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(APPLICANT_NAME_HEARING));
        assertTrue(documentContent.contains(DIVORCE_CASENO));
        assertTrue(documentContent.contains(SOLICITOR_REF_HEARING));
    }

    private JsonPath generateDocument(String jsonFileName, String url, String journeyType) {

        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).andReturn();

        int statusCode = jsonResponse.getStatusCode();
        assertEquals(200, statusCode);

        return jsonResponse.jsonPath();
    }

    private String getDocumentUrlOrDocumentBinaryUrl(String jsonFile, String url, String urlType, String documentType, String journeyType) {

        switch (documentType) {
            case MINI_FORM_A:
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_url");
                } else if (urlType.equals(BINARY_URL_TYPE)) {
                    url1 = jsonPathEvaluator.get("data.miniFormA.document_binary_url");
                }
                break;
            case "generalOrder":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_url");
                } else if (urlType.equals(BINARY_URL_TYPE)) {
                    url1 = jsonPathEvaluator.get("data.uploadOrder[0].value.DocumentLink.document_binary_url");
                }
                break;
            case "approvedConsentOrder":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.approvedConsentOrderLetter.document_url");
                } else if (urlType.equals(BINARY_URL_TYPE)) {
                    url1 = jsonPathEvaluator.get("data.approvedConsentOrderLetter.document_binary_url");
                }
                break;
            case "hearing":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.formC.document_url");
                } else if (urlType.equals(BINARY_URL_TYPE)) {
                    url1 = jsonPathEvaluator.get("data.formC.document_binary_url");
                }
                break;
            case "hearingG":
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                if (urlType.equals("document")) {
                    url1 = jsonPathEvaluator.get("data.formG.document_url");
                } else if (urlType.equals(BINARY_URL_TYPE)) {
                    url1 = jsonPathEvaluator.get("data.formG.document_binary_url");
                }
                break;
            default:
                jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
                break;
        }

        return url1;
    }

    private JsonPath accessGeneratedDocument(String url) {
        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeadersWithUserId())
            .when().get(url)
            .andReturn();

        int statusCode = jsonResponse.getStatusCode();
        assertEquals(200, statusCode);

        return jsonResponse.jsonPath();
    }

    private String fileRetrieveUrl(String url) {
        if (url != null && url.contains("dm-store:8080")) {
            return url.replace("http://dm-store:8080", dmStoreBaseUrl);
        }

        return url;
    }
}

