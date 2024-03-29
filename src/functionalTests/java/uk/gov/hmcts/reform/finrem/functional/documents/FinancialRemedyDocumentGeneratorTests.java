package uk.gov.hmcts.reform.finrem.functional.documents;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BINARY_URL_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@RunWith(SerenityRunner.class)
public class FinancialRemedyDocumentGeneratorTests extends IntegrationTestBase {

    private static final String APPLICANT_NAME = "Williams";
    private static final String CASE_NUMBER = "12345678";
    private static final String CASE_NUMBER_A = "123";
    private static final String SOLICITOR_FIRM = "Michael Jones & Partners";
    private static final String SOLICITOR_NAME = "Jane Smith";
    private static final String SOLICITOR_REF = "JAW052018";
    private static final String GENERAL_ORDER_JSON = "document-rejected-order1.json";
    private static final String MINI_FORM_A_JSON = "documentGeneratePayload1.json";
    private static final String MINI_FORM_A_CONTESTED_JSON = "generate-contested-form-A1.json";
    private static final String CONTESTED_HEARING_JSON = "validate-hearing-with-fastTrackDecision1.json";
    private static final String contestedDir = "/json/contested/";
    private static final String consentedDir = "/json/consented/";

    private String url1;
    private JsonPath jsonPathEvaluator;

    @Value("${cos.document.miniform.api}")
    private String generatorUrl;

    @Value("${document.management.store.baseUrl}")
    private String dmStoreBaseUrl;

    @Value("${document.rejected.order}")
    private String documentRejectedOrderUrl;

    @Value("${cos.document.contested.miniform.api}")
    private String generateContestedUrl;

    @Value("${cos.document.contested.draft.api}")
    private String generateContestedDraftUrl;

    @Value("${cos.document.hearing.api}")
    private String generateHearingUrl;

    @Value("${cos.hearing.notification.api}")
    private String generateHearingNotificationUrl;

    @Value("${case.orchestration.api}/hearing-order/store")
    private String hearingOrderStoreUrl;

    @Value("${case.orchestration.api}")
    private String caseOrchestration;

    @Test
    public void verifyContestedDraftDocumentGenerationShouldReturnOkResponseCode() {
        utils.validatePostSuccess(generateContestedUrl, CONTESTED_HEARING_JSON, contestedDir);
    }

    @Test
    public void verifyDocumentGenerationPostResponseContent() {
        generateDocument(MINI_FORM_A_JSON, generatorUrl, consentedDir);
    }

    @Test
    public void verifyRejectedOrderDocumentGenerationPostResponseContent() {

        JsonPath jsonPathEvaluator = generateDocument("rejected-consent-order.json", documentRejectedOrderUrl, consentedDir);

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

        generateDocument(CONTESTED_HEARING_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyContestedFormGDocumentGenerationPostResponseContent() {

        generateDocument(CONTESTED_HEARING_JSON, generateHearingUrl, contestedDir);
    }

    @Test
    public void verifyRejectedOrderGeneratedDocumentCanBeAccessedAndVerifyGetResponseContent() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERAL_ORDER_JSON, documentRejectedOrderUrl,
            "document", "generalOrder", consentedDir);

        JsonPath jsonPathEvaluator1 = accessGeneratedDocument(fileRetrieveUrl(documentUrl));

        assertTrue(jsonPathEvaluator1.get("originalDocumentName").toString().contains("GeneralOrder"));
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
    public void downloadDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_JSON, generatorUrl,
            BINARY_URL_TYPE, MINI_FORM_A, consentedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(SOLICITOR_FIRM));
        assertTrue(documentContent.contains(SOLICITOR_NAME));
        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(CASE_NUMBER));
        assertTrue(documentContent.contains(SOLICITOR_REF));
    }

    @Test
    public void downloadRejectOrderDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(GENERAL_ORDER_JSON, documentRejectedOrderUrl,
            BINARY_URL_TYPE, "generalOrder", consentedDir);
        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));
        assertNotNull(documentContent);
    }

    @Test
    public void downloadContestedDocumentAndVerifyContentAgainstOriginalJsonFileInput() {

        String documentUrl = getDocumentUrlOrDocumentBinaryUrl(MINI_FORM_A_CONTESTED_JSON, generateContestedUrl,
            BINARY_URL_TYPE, MINI_FORM_A, contestedDir);

        String documentContent = utils.downloadPdfAndParseToString(fileRetrieveUrl(documentUrl));

        assertTrue(documentContent.contains(APPLICANT_NAME));
        assertTrue(documentContent.contains(CASE_NUMBER_A));
    }

    private JsonPath generateDocument(String jsonFileName, String url, String journeyType) {
        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(jsonResponse.getStatusCode()));

        return jsonResponse.jsonPath();
    }

    private String getDocumentUrlOrDocumentBinaryUrl(String jsonFile, String url, String urlType, String documentType, String journeyType) {
        jsonPathEvaluator = generateDocument(jsonFile, url, journeyType);
        String path = null;
        switch (documentType) {
            case MINI_FORM_A -> path = "data.miniFormA";
            case "generalOrder" -> path = "data.uploadOrder[0].value.DocumentLink";
            case "approvedConsentOrder" -> path = "data.approvedConsentOrderLetter";
            case "hearing" -> path = "data.formC";
            case "hearingG" -> path = "data.formG";
            default -> {
            }
        }

        path = path == null ? null
            : urlType.equals("document") ? path + ".document_url"
            : urlType.equals(BINARY_URL_TYPE) ? path + ".document_binary_url"
            : path;

        if (path != null) {
            url1 = jsonPathEvaluator.get(path);
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
        return url != null && url.contains("dm-store:8080")
            ? url.replace("http://dm-store:8080", dmStoreBaseUrl)
            : url;
    }
}
