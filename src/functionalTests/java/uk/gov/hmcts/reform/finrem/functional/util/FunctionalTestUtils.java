package uk.gov.hmcts.reform.finrem.functional.util;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.finrem.functional.SolCCDServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.annotation.PostConstruct;

import static io.restassured.RestAssured.given;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class FunctionalTestUtils {

    @Autowired
    protected SolCCDServiceAuthTokenGenerator serviceAuthTokenGenerator;


    @Value("${user.id.url}")
    private String userId;

    @Value("${auth.idam.client.baseUrl}")
    private String baseServiceOauth2Url = "";

    private String serviceToken;
    private String clientToken;

    @PostConstruct
    public void init() {
        serviceToken = serviceAuthTokenGenerator.generateServiceToken();
        clientToken = serviceAuthTokenGenerator.getClientToken();
        if (userId == null || userId.isEmpty()) {
            createNewUser();
            userId = serviceAuthTokenGenerator.getUserId();
        }
    }

    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Headers getHeaders() {
        return getHeaders(clientToken);
    }

    public Headers getHeaders(String clientToken) {
        return Headers.headers(
            new Header("Authorization", clientToken),
            new Header("Content-Type", ContentType.JSON.toString()));
    }

    public Headers getHeadersWithUserId() {
        return getHeadersWithUserId(serviceToken, userId);
    }

    private Headers getHeadersWithUserId(String serviceToken, String userId) {
        return Headers.headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("user-roles", "caseworker-divorce"),
            new Header("user-id", userId));
    }


    public String downloadPdfAndParseToString(String documentUrl) {
        Response document = SerenityRest.given()
            .relaxedHTTPSValidation()
            .headers(getHeadersWithUserId())
            .when().get(documentUrl).andReturn();

        return parsePDFToString(document.getBody().asInputStream());
    }

    public String parsePDFToString(InputStream inputStream) {

        PDFParser parser;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        PDFTextStripper pdfStripper;
        String parsedText = "";

        try {
            parser = new PDFParser(inputStream);
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

    public void createNewUser() {
        given().headers("Content-type", "application/json")
            .relaxedHTTPSValidation()
            .body(getJsonFromFile("userCreation.json"))
            .post(baseServiceOauth2Url + "/testing-support/accounts");
    }
}
