package uk.gov.hmcts.reform.finrem.functional.migration;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.jsoup.select.Evaluator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

//@RunWith(SerenityRunner.class)
public class MigrationTests extends IntegrationTestBase {

    @Value("${migration.api}")
    private String migrationUrl;

    private static String CCD_MIGRATE_JSON = "ccd-migrate-request1.json";
    private static String CCD_MIGRATE_DONT_JSON = "ccd-migrate-dont-request1.json";
    private static String CCD_MIGRATE_DONT_JSON_NOAMOUNT_NOHWF = "ccd-migrate-dont-request1.json";

    //@Test
    public void verifyMigrationIsDone() {

        JsonPath jsonPathEvaluator = validateDoMigration(CCD_MIGRATE_JSON,migrationUrl);
        assertTrue(jsonPathEvaluator.get("data.amountToPay").toString()
                .equalsIgnoreCase("5000"));
        validatePostSuccess(CCD_MIGRATE_JSON,migrationUrl);

    }

    //@Test
    public void verifyMigrationIsNotRequired() {

        validatePostSuccess(CCD_MIGRATE_DONT_JSON,migrationUrl);
        verifyDontMigrateAssertions(validateDoMigration(CCD_MIGRATE_DONT_JSON,migrationUrl));

    }

    //@Test
    public void verifyMigrationIsNotRequiredWhenNoAmountToPayAndNoHWFInRequest() {

        validatePostSuccess(CCD_MIGRATE_DONT_JSON_NOAMOUNT_NOHWF,migrationUrl);
        verifyDontMigrateAssertions(validateDoMigration(CCD_MIGRATE_DONT_JSON_NOAMOUNT_NOHWF,migrationUrl));

    }

    private void verifyDontMigrateAssertions(JsonPath jsonPath) {
        assertNull("Should be null",jsonPath.get("data"));
        assertNull("Should be null",jsonPath.get("data_classification"));
        assertNull("Should be null",jsonPath.get("security_classification"));
        assertNull("Should be null",jsonPath.get("errors"));
        assertNull("Should be null",jsonPath.get("warnings"));
    }

    private void validatePostSuccess(String jsonFileName,String url) {

        System.out.println("url " + url);
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }

    private JsonPath validateDoMigration(String jsonFileName,String url) {
        Response jsonResponse = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .andReturn();
        return jsonResponse.jsonPath();
    }
}
