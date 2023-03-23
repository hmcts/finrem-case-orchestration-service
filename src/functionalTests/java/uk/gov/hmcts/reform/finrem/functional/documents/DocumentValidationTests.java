package uk.gov.hmcts.reform.finrem.functional.documents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PENSION_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPOND_TO_ORDER_DOCUMENTS;

@Slf4j
public class DocumentValidationTests extends IntegrationTestBase {

    private static final String RESPOND_TO_ORDER_SOLICITOR_JSON = "respond-to-order-solicitor.json";
    private static final String CONSENT_ORDER_JSON = "draft-consent-order.json";
    private static final String consentedDir = "/json/latestConsentedConsentOrder/";

    private ObjectMapper objectMapper = new ObjectMapper();
    private CallbackRequest callbackRequest;

    @Value("${cos.document.miniform.api}")
    private String generatorUrl;

    @Value("${cos.consentOrder.document.validation.api}")
    private String consentOrderFileCheckUrl;

    @Value("${cos.response.document.validation.api}")
    private String responseToOrderFileCheckUrl;

    @Value("${cos.pension.document.validation.api}")
    private String pensionDocumentFileCheckUrl;

    @Value("${cos.amend.consent.order.validation.api}")
    private String amendConsentOrderCollectionCheckUrl;

    private void setUpCaseDetails(String fileName) throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(consentedDir + fileName)) {
            callbackRequest = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void verifyDocumentForConsentOrderShouldReturnOkResponseCode() throws Exception {
        setUpCaseDetails(CONSENT_ORDER_JSON);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        data.put(CONSENT_ORDER, generateCaseDocument(CONSENT_ORDER_JSON));
        caseDetails.setData(data);
        callbackRequest.setCaseDetails(caseDetails);
        // call fileupload endpoint and assert
        Response response = utils.getResponseData(responseToOrderFileCheckUrl, callbackRequest);
        HttpStatus responseHttpStatus = HttpStatus.valueOf(response.getStatusCode());

        assertEquals(HttpStatus.OK, responseHttpStatus);
        assertNull(response.jsonPath().get("errors"));
    }

    @Test
    public void verifyDocumentForPensionCollectionShouldReturnOkResponseCode() throws Exception {
        setUpCaseDetails(CONSENT_ORDER_JSON);
        setPensionCollectionData();
        // call fileupload endpoint and assert
        Response response = utils.getResponseData(pensionDocumentFileCheckUrl, callbackRequest);
        HttpStatus responseHttpStatus = HttpStatus.valueOf(response.getStatusCode());

        assertEquals(HttpStatus.OK, responseHttpStatus);
        assertNull(response.jsonPath().get("errors"));
    }

    @Test
    public void verifyDocumentForRespondToOrderShouldReturnOkResponseCode() throws Exception {
        setUpCaseDetails(RESPOND_TO_ORDER_SOLICITOR_JSON);
        setResponseToOrderDocument();
        // call fileupload check endpoint
        Response response = utils.getResponseData(consentOrderFileCheckUrl, callbackRequest);
        HttpStatus responseHttpStatus = HttpStatus.valueOf(response.getStatusCode());

        assertEquals(HttpStatus.OK, responseHttpStatus);
        assertNull(response.jsonPath().get("errors"));
    }

    @Test
    public void verifyDocumentForConsentOrderCollectionShouldReturnOkResponseCode() throws Exception {
        setUpCaseDetails("amend-consent-order-by-caseworker.json");
        setAmendConsentOrderCollectionData();
        // call fileupload endpoint and assert
        Response response = utils.getResponseData(amendConsentOrderCollectionCheckUrl, callbackRequest);
        HttpStatus responseHttpStatus = HttpStatus.valueOf(response.getStatusCode());

        assertEquals(HttpStatus.OK, responseHttpStatus);
        assertNull(response.jsonPath().get("errors"));
    }

    private CaseDocument generateCaseDocument(String fileName) {
        // generate pdf document to set it as consent order
        JsonPath jsonPathEvaluator = generateDocument(fileName, generatorUrl, consentedDir);

        return getCaseDocument(jsonPathEvaluator);
    }

    private io.restassured.path.json.JsonPath generateDocument(String jsonFileName, String url, String journeyType) {
        Response jsonResponse = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(utils.getHeaders())
            .body(utils.getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).andReturn();

        HttpStatus responseHttpStatus = HttpStatus.valueOf(jsonResponse.getStatusCode());
        assertEquals(HttpStatus.OK, responseHttpStatus);

        return jsonResponse.jsonPath();
    }

    private CaseDocument getCaseDocument(JsonPath jsonPathEvaluator) {
        Object object = jsonPathEvaluator.get("data.miniFormA");
        return convertToCaseDocument(object);
    }

    private CaseDocument convertToCaseDocument(Object object) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(object, CaseDocument.class);
    }

    private List<RespondToOrderData> convertToRespondToOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(object, new TypeReference<List<RespondToOrderData>>() {
        });
    }

    private List<PensionTypeCollection> convertToPensionCollectionDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(object, new TypeReference<List<PensionTypeCollection>>() {});
    }

    private void setPensionCollectionData() {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        Object pensionObject = data.get(PENSION_DOCS_COLLECTION);
        List<PensionTypeCollection> respondToOrderData = convertToPensionCollectionDataList(pensionObject);
        PensionType typedCaseDocument = respondToOrderData.get(0).getTypedCaseDocument();
        typedCaseDocument.setPensionDocument(generateCaseDocument(CONSENT_ORDER_JSON));
        data.put(PENSION_DOCS_COLLECTION, respondToOrderData);
        caseDetails.setData(data);
        callbackRequest.setCaseDetails(caseDetails);
    }

    private void setResponseToOrderDocument() {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        Object respondToOrderDocumentsObject = data.get(RESPOND_TO_ORDER_DOCUMENTS);
        List<RespondToOrderData> respondToOrderData = convertToRespondToOrderDataList(respondToOrderDocumentsObject);
        RespondToOrder respondToOrder = respondToOrderData.get(0).getRespondToOrder();
        respondToOrder.setDocumentLink(generateCaseDocument(RESPOND_TO_ORDER_SOLICITOR_JSON));
        data.put(RESPOND_TO_ORDER_DOCUMENTS, respondToOrderData);
        caseDetails.setData(data);
        callbackRequest.setCaseDetails(caseDetails);
    }

    private void setAmendConsentOrderCollectionData() throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        Object object = data.get(AMENDED_CONSENT_ORDER_COLLECTION);
        List<AmendedConsentOrderData> amendedConsentOrders = convertToAmendedConsentOrderDataList(object);
        int length = amendedConsentOrders.size();
        AmendedConsentOrder amendedConsentOrderData = amendedConsentOrders.get(length - 1).getConsentOrder();
        amendedConsentOrderData.setAmendedConsentOrder(generateCaseDocument(CONSENT_ORDER_JSON));
        data.put(AMENDED_CONSENT_ORDER_COLLECTION, amendedConsentOrders);
        caseDetails.setData(data);
        callbackRequest.setCaseDetails(caseDetails);
    }

    private List<AmendedConsentOrderData> convertToAmendedConsentOrderDataList(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.convertValue(object, new TypeReference<List<AmendedConsentOrderData>>() {
        });
    }
}
