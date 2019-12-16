package uk.gov.hmcts.reform.finrem.functional.bulkscan;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
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

    private static final String FORM_JSON_PATH = "/json/bulkscan/basic.json";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorisation";
    private static String VALID_BODY;
    private static String VALIDATION_END_POINT = "/forms/{form-type}/validate-ocr";
    private static String TRANSFORMATION_END_POINT = "/transform-exception-record";
    private static String UPDATE_END_POINT = "/update-case";

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);
        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);

        Response forValidationEndpoint = responseForValidationEndpoint(token, VALIDATION_END_POINT);


        assert forValidationEndpoint.getStatusCode() == 200 : "Service is not authorised to OCR validation "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForValidationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);

        Response forValidationEndpoint = responseForValidationEndpoint(token,VALIDATION_END_POINT);

        assert forValidationEndpoint.getStatusCode() == 403 : "Not matching with expected Error code "
            + forValidationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);
        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forTransformationEndpoint.getStatusCode();

    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForTransformationEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);

        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);

        Response forTransformationEndpoint = responseForEndpoint(token, TRANSFORMATION_END_POINT);

        assert forTransformationEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
            + forTransformationEndpoint.getStatusCode();
    }

    @Test
    public void shouldGetSuccessfulResponsesWhenUsingWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanTransformationAndUpdateMicroService);

        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);
        Response forUpdateEndpoint = responseForEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 200 : "Service is not authorised to transform OCR data to case"
            + forUpdateEndpoint.getStatusCode();

    }

    @Test
    public void shouldGetServiceDeniedWhenUsingNonWhitelistedServiceForUpdateEndPoint()  throws Exception {
        String token = idamUtilsS2SAuthorization.generateUserTokenWithValidMicroService(bulkScanValidationMicroService);

        VALID_BODY = loadResourceAsString(FORM_JSON_PATH);

        Response forUpdateEndpoint = responseForEndpoint(token, UPDATE_END_POINT);

        assert forUpdateEndpoint.getStatusCode() == 403 : "Not matching with expected error Code "
           + forUpdateEndpoint.getStatusCode();
    }

    private Response responseForEndpoint(String token, String endpointName) {
        Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseUrl + endpointName);
        return response;
    }

    private Response responseForValidationEndpoint(String token, String endpointName) {
        Response  response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header(SERVICE_AUTHORISATION_HEADER, token)
            .relaxedHTTPSValidation()
            .body(VALID_BODY)
            .post(cosBaseUrl + endpointName);
        return response;
    }

    private static String loadResourceAsString(final String filePath) throws Exception {
        URL url = BulkScanIntegrationTest.class.getClassLoader().getResource(filePath);

        if (url == null) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        return new String(Files.readAllBytes(Paths.get(url.toURI())));
    }
}
