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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class FunctionalTestUtils {

    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    @Value("${user.id.url}")
    private String userId;
    @Value("${idam.username}")
    private String idamUserName;
    @Value("${idam.userpassword}")
    private String idamUserPassword;
    @Value("${idam.s2s-auth.microservice}")
    private String microservice;
    @Autowired
    private IdamUtils idamUtils;
    private String token;


    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getJsonFromFile(String fileName , String directory) {
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
                new Header("ServiceAuthorization",   tokenGenerator.generate()),
                new Header("user-roles", "caseworker-divorce"),
                new Header("user-id", userId));
    }

    public Headers getHeaders() {
        return Headers.headers(

                new Header("Authorization", "Bearer "
                 + idamUtils.generateUserTokenWithNoRoles(idamUserName, idamUserPassword)),
                //new Header("Authorization", "Bearer " + idamUtils.getClientAuthToken()),
                new Header("Content-Type", ContentType.JSON.toString()));
    }

    public String getAuthoToken() {
        return idamUtils.generateUserTokenWithNoRoles(idamUserName,idamUserPassword);
    }

    public Headers getHeader() {

        return Headers.headers(
                new Header("Authorization", "Bearer "
                        + idamUtils.generateUserTokenWithNoRoles(idamUserName, idamUserPassword)));

    }


    public Headers getNewHeaders() {
        return Headers.headers(
                new Header("Content-Type", "application/json"));
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

}
