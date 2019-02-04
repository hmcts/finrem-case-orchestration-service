package uk.gov.hmcts.reform.finrem.functional.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class MigrationTests extends IntegrationTestBase {

    private JsonParser parser = new JsonParser();
    private JsonElement fromFile;
    private JsonElement fromResponse;
    private ObjectMapper mapper;
    private JsonNode tree1;
    private JsonNode tree2;
    private  Gson gson= new Gson();


    @Test
    public void verifyMigrationRequestShouldReturnOkResponseCode() {

        validatePostmigrationSuccess("ccd-request-solicitor-rSolicitor-address.json");
    }

    @Test
    public void verifyOriginalDataisNotAffectedAfterMigration()
    {
       verifyOriginalDataIsNotAffected("ccd-request-solicitor-rSolicitor-address.json");
       verifyOriginalDataIsNotAffected("ccd-request-solicitor-respondent-address.json");

    }

    public void verifyOriginalDataIsNotAffected(String fileName) {

        Response response = getResponseForMigration(fileName);

        try {
            fromResponse = parser.parse(response.prettyPrint()
                    .replace("\\", ""))
                    .getAsJsonObject()
                    .get("data");

            fromFile = gson.fromJson(getJsonFromFileWithPropertiesRemoved(fileName).toString(), JsonElement.class);

            mapper = new ObjectMapper();

            tree1 = mapper.readTree(fromFile.getAsJsonObject().toString());
            tree2 = mapper.readTree(fromResponse.getAsJsonObject().toString());

            assertTrue(tree1.equals(tree2));

        } catch (Throwable t) {
            throw new Error(t);
        }
    }


    public void validatePostmigrationSuccess(String jsonFileName) {

        Response response = getResponseForMigration(jsonFileName);
        ResponseBody body = response.getBody();
        String bodyAsString = body.asString();

        assertFalse(bodyAsString.contains("solicitorAddress1"));
        assertFalse(bodyAsString.contains("solicitorAddress2"));
        assertFalse(bodyAsString.contains("solicitorAddress3"));
        assertFalse(bodyAsString.contains("solicitorAddress4"));
        assertFalse(bodyAsString.contains("solicitorAddress5"));

        assertFalse(bodyAsString.contains("rSolicitorAddress1"));
        assertFalse(bodyAsString.contains("rSolicitorAddress2"));
        assertFalse(bodyAsString.contains("rSolicitorAddress3"));
        assertFalse(bodyAsString.contains("rSolicitorAddress4"));
        assertFalse(bodyAsString.contains("rSolicitorAddress5"));

        assertFalse(bodyAsString.contains("respondentAddress1"));
        assertFalse(bodyAsString.contains("respondentAddress2"));
        assertFalse(bodyAsString.contains("respondentAddress3"));
        assertFalse(bodyAsString.contains("respondentAddress4"));
        assertFalse(bodyAsString.contains("respondentAddress5"));
        assertFalse(bodyAsString.contains("respondentAddress6"));

    }


    public Response getResponseForMigration(String jsonFileName) {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post();

    }

    public JSONObject getJsonFromFileWithPropertiesRemoved(String fileName) throws JSONException
    {
        JSONObject jo2 = new JSONObject(utils.getJsonFromFile(fileName));

        try{

        jo2= (JSONObject) jo2.get("case_details");
        jo2= (JSONObject) jo2.get("case_data");
        jo2.remove("solicitorAddress1");
        jo2.remove("solicitorAddress2");
        jo2.remove("solicitorAddress3");
        jo2.remove("solicitorAddress4");
        jo2.remove("solicitorAddress5");
        jo2.remove("rSolicitorAddress1");
        jo2.remove("rSolicitorAddress2");
        jo2.remove("rSolicitorAddress3");
        jo2.remove("rSolicitorAddress4");
        jo2.remove("rSolicitorAddress5");
        jo2.remove("respondentAddress1");
        jo2.remove("respondentAddress2");
        jo2.remove("respondentAddress3");
        jo2.remove("respondentAddress4");
        jo2.remove("respondentAddress5");

    } catch (Throwable t) {
        throw new Error(t);
    }
        return jo2;
    }


// "estimateLengthOfHearing": "10","orderRefusalNotEnough": ["reason1"],,
//            "orderRefusalNotEnoughOther": "test",
//            "whenShouldHearingTakePlace": "today",
//            "whereShouldHearingTakePlace": "EZ801",            "orderRefusalOther": "test1", otherHearingDetails

}

