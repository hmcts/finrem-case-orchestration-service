package uk.gov.hmcts.reform.finrem.functional.util;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ServiceUtils {

    @Value("${evidence.management.client.api}")
    private String evidenceManagementClientBaseUrl;

    @Autowired
    private FunctionalTestUtils functionalTestUtils;

    public Map<String,String> uploadFileToEMStore(String fileToUpload, String fileContentType) throws JSONException {
        File file = null;
        try {
            file = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(fileToUpload)).toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Response response = SerenityRest.given()
                .headers(functionalTestUtils.getHeader())
                .multiPart("file", file, fileContentType)
                .post(evidenceManagementClientBaseUrl.concat("/upload"))
                .andReturn();
        org.junit.Assert.assertEquals(HttpStatus.OK.value(), response.statusCode());

        JSONArray responseJson = new JSONArray(response.body().asString());

        JSONObject fileUploadResponse = (JSONObject) responseJson.get(0);
        Map<String,String> uploadedDocument = new HashMap<>();
        uploadedDocument.put("document_url",fileUploadResponse.get("fileUrl").toString());
        uploadedDocument.put("document_filename",fileUploadResponse.get("fileName").toString());
        uploadedDocument.put("document_binary_url",fileUploadResponse.get("fileUrl").toString()+"/binary");

        return uploadedDocument;
    }
}
