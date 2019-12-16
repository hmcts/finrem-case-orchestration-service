package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class BulkScanIntegrationTest {

    @Autowired
    private IdamUtils idamUtilsS2SAuthorization;

    @Value("${auth.provider.bulkscan.validate.microservice}")
    private String bulkScanValidationMicroService;

    @Value("${auth.provider.bulkscan.update.microservice}")
    private String bulkScanTransformationAndUpdateMicroService;

    @Value("${case.orchestration.api}")
    private String cosBaseUrl;

    private static final String FORM_JSON_PATH = "json/bulkscan/basic.json";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorisation";
    private static String body;
    private static String token;
    private static String VALIDATION_END_POINT = "/forms/formA/validate-ocr";
    private static String TRANSFORMATION_END_POINT = "/transform-exception-record";
    private static String UPDATE_END_POINT = "/update-case";

    @Before
    public void setup() throws Exception {
        body = loadValidBody();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint() {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);

        Response forValidationEndpoint = responseForValidationEndpoint(token);

        assert forValidationEndpoint.getStatusCode() == 200 : "Service is not authorised to OCR validation "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        body = loadValidBody();

        Response forValidationEndpoint = responseForValidationEndpoint(token);

        assert forValidationEndpoint.getStatusCode() == 403 : "Not matching with expected Error code "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        body = loadValidBody();
        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        body = loadValidBody();

        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);
        body = loadValidBody();

        Response forUpdateEndpoint = responseForEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forUpdateEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
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

    private static String loadValidBody() throws Exception {
        URL url = BulkScanIntegrationTest.class.getClassLoader().getResource(FORM_JSON_PATH);
        return new String(Files.readAllBytes(Paths.get(url.toURI())));
    }
}
