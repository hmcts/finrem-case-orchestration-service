package uk.gov.hmcts.reform.finrem.caseorchestration.contracts;


import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.contracts.utils.RequestJsonUtil;

import static org.springframework.test.util.AssertionErrors.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "SIDAM_Provider", port = "8880")
@SpringBootTest({
    "idam.url: http://localhost:8889"
})
public class CaseDataControllerPact {


    private final String AUTH_TOKEN = "someAuthorizationToken";

    @Pact(state = "Finrem returns defaults for consented",
        provider = "Finrem_Cos_Service", consumer = "ccd_finrem")
    public RequestResponsePact finremCosConsentedDefaultsPact(PactDslWithProvider packBuilder) throws JSONException {

        return packBuilder
            .given("Finrem service provides default values for consented")
            .uponReceiving("GET request for isAdmin ")
            .path("/case-orchestration/consented/set-defaults")
            .matchHeader("Authorization",AUTH_TOKEN)
            .method("POST")
            .body(setDefaultConsentedRequestBody())
            .willRespondWith()
            .status(200)
            .body(setDefaultConsentedResponseBody())
            .toPact();
    }

    @Pact(state = "Finrem returns defaults for contested",
        provider = "Finrem_Cos_Service", consumer = "ccd_finrem")
    public RequestResponsePact finremCosContestedDefaultsPact(PactDslWithProvider packBuilder) throws JSONException {

        return packBuilder
            .given("Finrem service provides default values for contested")
            .uponReceiving("GET request for isAdmin ")
            .path("/case-orchestration/contested/set-defaults")
            .matchHeader("Authorization",AUTH_TOKEN)
            .method("POST")
            .body(setDefaultConsentedRequestBody())
            .willRespondWith()
            .status(200)
            .body(setDefaultConsentedResponseBody())
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "finremCosConsentedDefaultsPact")
    public void verifyPactForConsentedDefaults() throws JSONException {

        Response response = SerenityRest.given().log().all()
            .header("Authorization",AUTH_TOKEN)
            .header("Content-type","application/json")
            .body(setDefaultConsentedRequestBody().toString())
            .when()
            .post("http://localhost:8880/case-orchestration/consented/set-defaults")
            .thenReturn();
        System.out.println("Response : "+response.prettyPrint());
        assertTrue("Response Code "+response.getStatusCode(),response.getStatusCode() == 200);
    }

    @Test
    @PactTestFor(pactMethod = "finremCosContestedDefaultsPact")
    public void verifyPactForContestedDefaults() throws JSONException {

        Response response = SerenityRest.given().log().all()
            .header("Authorization",AUTH_TOKEN)
            .header("Content-type","application/json")
            .body(setDefaultConsentedRequestBody().toString())
            .when()
            .post("http://localhost:8880/case-orchestration/contested/set-defaults")
            .thenReturn();
        System.out.println("Response : "+response.prettyPrint());
        assertTrue("Response Code "+response.getStatusCode(),response.getStatusCode() == 200);
    }

    private JSONObject setDefaultConsentedRequestBody() throws JSONException {
        JSONObject obj = new JSONObject(RequestJsonUtil.getRequestDocument().jsonString());
       return  obj;
    }

    private JSONObject setDefaultConsentedResponseBody() throws JSONException {
        JSONObject obj = new JSONObject(RequestJsonUtil.getResponseDocument().jsonString());
//        obj.put()
        JSONObject data =  obj.getJSONObject("data");
        data.put("isAdmin","No");
        data.put("applicantRepresented","Yes");

        return  obj;
    }


}
