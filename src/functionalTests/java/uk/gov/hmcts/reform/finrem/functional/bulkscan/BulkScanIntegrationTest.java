package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

@RunWith(SerenityRunner.class)
@Slf4j
public class BulkScanIntegrationTest extends IntegrationTestBase {

    @Value("${auth.provider.bulkscan.validate.microservice}")
    private String bulkScanValidationMicroService;

    @Value("${auth.provider.bulkscan.update.microservice}")
    private String bulkScanTransformationAndUpdateMicroService;

    @Value("${case.orchestration.api-bsp}")
    private String cosBaseUrl;

    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorisation";
    private static String body;
    private static String VALIDATION_END_POINT = "/forms/formA/validate-ocr";
    private static String TRANSFORMATION_END_POINT = "/transform-exception-record";
    private static String UPDATE_END_POINT = "/update-case";

    @Before
    public void setup() {
        body = loadValidBody();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint() {
        String token = utils.getS2SToken(bulkScanValidationMicroService);

        Response forValidationEndpoint = responseForValidationEndpoint(token);

        assert forValidationEndpoint.getStatusCode() == 200 : "Service is not authorised to OCR validation "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = utils.getS2SToken(bulkScanTransformationAndUpdateMicroService);
        body = loadValidBody();

        Response forValidationEndpoint = responseForValidationEndpoint(token);

        assert forValidationEndpoint.getStatusCode() == 403 : "Not matching with expected Error code "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = utils.getS2SToken(bulkScanTransformationAndUpdateMicroService);

        body = loadValidBody();
        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = utils.getS2SToken(bulkScanValidationMicroService);
        body = loadValidBody();

        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = utils.getS2SToken(bulkScanTransformationAndUpdateMicroService);
        body = loadValidBody();

        Response forUpdateEndpoint = responseForEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forUpdateEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = utils.getS2SToken(bulkScanValidationMicroService);
        body = loadValidBody();

        Response forUpdateEndpoint = responseForEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
           + forUpdateEndpoint.getStatusCode();
    }

    private Response responseForEndpoint(String token, String endpointName) {
        return SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(body)
            .post(cosBaseUrl + endpointName);
    }

    private Response responseForValidationEndpoint(String token) {
        return SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(body)
            .post(cosBaseUrl + VALIDATION_END_POINT);
    }

    private String loadValidBody() {
        return "{\n"
                + "  \"case_type_id\": \"FinancialRemedyContested\",\n"
                + "  \"id\": \"LV481297\",\n"
                + "  \"po_box\": \"PO 17\",\n"
                + "  \"form_type\": \"formA\",\n"
                + "  \"ocr_data_fields\": [\n"
                + "    { \"name\": \"PetitionerFirstName\", \"value\": \"John\" },\n"
                + "    { \"name\": \"PetitionerLastName\", \"value\": \"Smith\" }\n"
                + "  ]\n"
                + "}";
    }
}
