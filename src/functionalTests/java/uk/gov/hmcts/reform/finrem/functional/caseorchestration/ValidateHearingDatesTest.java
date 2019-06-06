package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.json.Json;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class ValidateHearingDatesTest extends IntegrationTestBase {

    @Value("${cos.validate.hearing}")
    private String validateHearing;

    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";
    private JsonPath jsonPathEvaluator;

    @Test
    public void verifyShouldReturnBadRequestWhenCaseDataIsMissingInRequest() {

        assertEquals(400, getStatusCode(validateHearing, "empty-casedata1.json",consentedDir));
        assertEquals("Some server side exception occurred. Please check logs for details",
            getResponse(validateHearing, "empty-casedata1.json",consentedDir).body().asString());
    }

    @Test
    public void verifyShouldThrowErrorWhenIssueDateAndHearingDateAreEmpty() {
        validatePostSuccess(validateHearing, "pba-validate.json" , consentedDir);
        assertEquals("Issue Date , fast track decision or hearingDate is empty",
                getResponse(validateHearing, "pba-validate.json",consentedDir).jsonPath().get("errors[0]"));
    }

    @Test
    public void verifyShouldThrowWarningsWhenNotFastTrackDecision() {
        validatePostSuccess(validateHearing,
                "validate-hearing-withoutfastTrackDecision1.json" , contestedDir);
        assertEquals("Date of the hearing must be between 12 and 16 weeks.",
                getResponse(validateHearing, "validate-hearing-withoutfastTrackDecision1.json",
                        contestedDir).jsonPath().get("warnings[0]"));
    }

    @Test
    public void verifyshouldThrowWarningsWhenFastTrackDecision() {
        validatePostSuccess(validateHearing,
                "validate-hearing-with-fastTrackDecision1.json" , contestedDir);
        assertEquals("Date of the Fast Track hearing must be between 6 and 10 weeks.",
                getResponse(validateHearing, "validate-hearing-with-fastTrackDecision1.json",
                        contestedDir).jsonPath().get("warnings[0]"));
    }

    @Test
    public void verifyShouldSuccessfullyValidate() {
        validatePostSuccess(validateHearing,
                "validate-hearing-successfully1.json" , contestedDir);
        Assert.assertNull(
                getResponse(validateHearing, "validate-hearing-successfully1.json",
                        contestedDir).jsonPath().get("warnings"));
    }

    private void validatePostSuccess(String url,String jsonFileName,  String journeyType) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }


    private int getStatusCode( String url, String jsonFileName,String journeyType) {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).getStatusCode();
    }

    private Response getResponse(String url,  String jsonFileName, String journeyType) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile(jsonFileName, journeyType))
                .when().post(url).andReturn();

    }


}
