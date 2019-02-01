package uk.gov.hmcts.reform.finrem.functional.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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


    @Test
    public void verifyMigrationRequestShouldReturnOkResponseCode() {

        validatePostmigrationSuccess("ccd-request.json");
    }

    @Test
    public void verifyOriginalDataIsNotAffected() {

        Response response = getResponseForMigration("ccd-request.json");

        try {
            fromResponse = parser.parse(response.prettyPrint()
                           .replace("\\", ""))
                           .getAsJsonObject()
                           .get("data");

            fromFile = parser.parse(utils.getJsonFromFile("ccd-request_CheckDataReturnedCorrectly.json"))
                             .getAsJsonObject()
                             .get("case_details")
                             .getAsJsonObject()
                             .get("case_data");

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
        assertFalse(bodyAsString.contains("solicitorAddress3"));
        assertFalse(bodyAsString.contains("solicitorAddress3"));

        assertFalse(bodyAsString.contains("rSolicitorAddress1"));
        assertFalse(bodyAsString.contains("rSolicitorAddres2"));
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

}

