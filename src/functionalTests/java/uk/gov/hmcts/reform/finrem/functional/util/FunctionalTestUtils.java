package uk.gov.hmcts.reform.finrem.functional.util;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
@Slf4j
@RequiredArgsConstructor
public class FunctionalTestUtils {

    private final ServiceAuthTokenGenerator tokenGenerator;
    private final IdamUtils idamUtils;

    @Value("${user.id.url}")
    private String userId;

    @Value("${idam.username}")
    private String idamUserName;

    @Value("${idam.userpassword}")
    private String idamUserPassword;

    public String getJsonFromFile(String fileName, String directory) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource(directory + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Headers getHeadersWithUserId() {
        return Headers.headers(
            new Header(SERVICE_AUTHORISATION_HEADER, tokenGenerator.generate()),
            new Header("user-roles", "caseworker-divorce"),
            new Header("user-id", userId));
    }

    public Headers getHeaders() {
        return Headers.headers(
            new Header(AUTHORIZATION_HEADER, "Bearer "
                + idamUtils.generateUserTokenWithNoRoles(idamUserName, idamUserPassword)),
            new Header("Content-Type", ContentType.JSON.toString()));
    }

    public String getS2SToken(String callerMicroservice) {
        return idamUtils.generateServiceTokenWithValidMicroservice(callerMicroservice);
    }

    public Headers getHeader() {
        return Headers.headers(
            new Header(AUTHORIZATION_HEADER, "Bearer "
                + idamUtils.generateUserTokenWithNoRoles(idamUserName, idamUserPassword)));
    }

    public String downloadPdfAndParseToString(String documentUrl) {
        Response document = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeadersWithUserId())
            .when().get(documentUrl).andReturn();

        return parsePdfToString(document.getBody().asInputStream());
    }

    public String parsePdfToString(InputStream inputStream) {
        PDFParser parser;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        PDFTextStripper pdfStripper;
        String parsedText;

        try {
            parser = new PDFParser((RandomAccessRead) inputStream);
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            parsedText = pdfStripper.getText(pdDoc);
        } catch (Throwable t) {
            t.printStackTrace();

            try {
                if (cosDoc != null) {
                    cosDoc.close();
                }

                if (pdDoc != null) {
                    pdDoc.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw new Error(t);
        }

        return parsedText;
    }

    public void validatePostSuccess(String url, String filename, String journeyType) {
        int statusCode = getResponse(url, filename, journeyType).getStatusCode();
        assertEquals(HttpStatus.OK.value(), statusCode);
    }

    public JsonPath getResponseData(String url, String jsonBody, String dataPath) {
        Response response = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeader())
            .contentType("application/json")
            .body(jsonBody)
            .when().post(url)
            .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getStatusCode()));

        JsonPath jsonPath = response.jsonPath().setRoot(dataPath);
        return jsonPath;
    }

    public JsonPath getResponseData(String url, String filename, String journeyType, String dataPath) {
        Response response = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeader())
            .contentType("application/json")
            .body(getJsonFromFile(filename, journeyType))
            .when().post(url)
            .andReturn();

        assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getStatusCode()));

        JsonPath jsonPath = response.jsonPath().setRoot(dataPath);
        return jsonPath;
    }

    public Response getResponseData(String url, CallbackRequest callbackRequest) {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeader())
            .contentType("application/json")
            .body(callbackRequest)
            .when().post(url)
            .andReturn();
    }

    public Response getResponse(String url, String filename, String journeyType) {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeader())
            .contentType("application/json")
            .body(getJsonFromFile(filename, journeyType))
            .when().post(url).andReturn();
    }

    public int getStatusCode(String url, String jsonFileName, String journeyType) {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeaders())
            .body(getJsonFromFile(jsonFileName, journeyType))
            .when().post(url).getStatusCode();
    }
}
