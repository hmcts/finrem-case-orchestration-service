package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;
import uk.gov.hmcts.reform.finrem.functional.util.ServiceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RunWith(SerenityRunner.class)
public class ConsentOrderApprovedTest extends IntegrationTestBase {

    @Autowired
    private FunctionalTestUtils functionalTestUtils;

    @Autowired
    private ServiceUtils serviceUtils;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final String CONTESTED_DIR = "/json/contested/";
    private final String CONSENTED_DIR = "/json/consented/";


    @Value("${cos.consentOrder.approved}")
    private String consentOrderApprovedUrl;


    @Test
    public void verifyConsentOrderApprovedForConsentedCase(){
        CallbackRequest callbackRequest = null;
        InputStream resourceAsStream = null;
        resourceAsStream = getClass().getResourceAsStream(CONSENTED_DIR+"approved-consent-order.json");

        Map<String,String> uploadedDoc = null;
        try {
            uploadedDoc = serviceUtils.uploadFileToEMStore("fileTypes/sample.pdf","application/pdf");
        } catch (JSONException e) {
            throw new RuntimeException("Exception uploading file to evidence store ",e);
        }

        DocumentContext documentContext = JsonPath.parse(resourceAsStream).set("$..latestConsentOrder",uploadedDoc);
        documentContext.set("$..pensionCollection[0].value.uploadedDocument",uploadedDoc);
        try {
            callbackRequest = objectMapper.readValue(documentContext.jsonString(), CallbackRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response response = functionalTestUtils.getResponseData(consentOrderApprovedUrl,callbackRequest);
        Assert.assertEquals("Request failed " + response.getStatusCode(), 200, response.getStatusCode());
    }
}
