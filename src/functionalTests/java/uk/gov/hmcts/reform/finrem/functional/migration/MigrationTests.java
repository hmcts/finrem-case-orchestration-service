package uk.gov.hmcts.reform.finrem.functional.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONObject;
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
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode tree1;
    private JsonNode tree2;
    private Gson gson = new Gson();


    @Test
    public void verifyMigrationRequestShouldReturnOkResponseCode() {

        validatePostMigrationSuccess("ccd-request-solicitor-rSolicitor-address.json");
    }

    @Test
    public void verifyOriginalDataisNotAffectedAfterMigration() {

        verifyOriginalDataIsNotAffected("ccd-request-solicitor-rSolicitor-address.json",
                "ccd-request-solicitor-rSolicitor-address_ValidateOutput.json");

        verifyOriginalDataIsNotAffected("ccd-request-solicitor-respondent-address.json",
                "ccd-request-solicitor-respondent-address_ValidateOutput.json");

    }

    public void verifyOriginalDataIsNotAffected(String originalFileName, String expectedOutputFileName) {

        Response response = getResponseForMigration(originalFileName);

        try {
            fromResponse = parser.parse(response.prettyPrint()
                    .replace("\\", ""))
                    .getAsJsonObject()
                    .get("data");

            JSONObject jo2 = new JSONObject(utils.getJsonFromFile(expectedOutputFileName));
            jo2 = (JSONObject) jo2.get("case_details");
            jo2 = (JSONObject) jo2.get("case_data");

            fromFile = gson.fromJson(jo2.toString(), JsonElement.class);

            tree1 = mapper.readTree(fromFile.getAsJsonObject().toString());
            tree2 = mapper.readTree(fromResponse.getAsJsonObject().toString());

            assertTrue(tree1.equals(tree2));

        } catch (Throwable t) {
            throw new Error(t);
        }
    }


    public void validatePostMigrationSuccess(String jsonFileName) {

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
                .headers(utils.getNewHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post();

    }

}

