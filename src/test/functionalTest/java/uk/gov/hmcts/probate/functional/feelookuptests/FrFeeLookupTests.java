package uk.gov.hmcts.probate.functional.feelookuptests;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.containsString;

@RunWith(SerenityRunner.class)
public class FrFeeLookupTests extends IntegrationTestBase {

    private static final String FEE_LOOKUP_URL = "/case-orchestration/fee-lookup";


    @Test
    public void verifyFeeAmount() {
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body(utils.getJsonFromFile("feelookup.json"))
                .when().post(FEE_LOOKUP_URL).then().statusCode(200)
                .and().body("data.feeAmountToPay", equalToIgnoringCase("50.00"));

    }

    @Test
    public void verifyEmptyRequest() {
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .body("")
                .when().post(FEE_LOOKUP_URL).then().statusCode(400)
                .and().body("message", containsString("Required request body is missing:"));

    }

}
