package uk.gov.hmcts.reform.finrem.functional.util;

import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class FunctionalTestUtils {


    @Autowired
    private IdamUtils idamUtils;

    @Value("${idam.username}")
    private String idamUserName;

    @Value("${idam.userpassword}")
    private String idamUserPassword;

    @Value("${user.id.url}")
    private String userId;

    @Value("${idam.api.url}")
    private String baseServiceOauth2Url = "";


    private String serviceToken;
    private String clientToken;


    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Headers getNewHeaders() {
        return Headers.headers(
                new Header("Authorization", idamUtils.generateUserTokenWithNoRoles(idamUserName, idamUserPassword)),
                new Header("Content-Type", ContentType.JSON.toString()));
    }


}
