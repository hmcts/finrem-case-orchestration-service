package uk.gov.hmcts.reform.finrem.functional.payments;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class PaymentServiceTests extends IntegrationTestBase {

    @Value("${user.id.url}")
    private String userId;

    @Value("${cos.payment.fee.lookup.api}")
    private String feeLookup;

    @Value("${cos.payment.pba.validate.api}")
    private String pbaValidate;

    @Value("${cos.payment.pba.api}")
    private String pbaPayment;

    @Value("${cos.pba.account.active}")
    private String pbaAccountActive;

    @Value("${cos.pba.account.inactive}")
    private String pbaAccountInActive;

    @Value("${cos.payment.pba.confirmation.api}")
    private String pbaConfirmation;

    @Value("${idam.username}")
    private String idamUserName;

    @Value("${idam.userpassword}")
    private String idamUserPassword;

    @Value("${idam.api.secret}")
    private String idamSecret;

    @Value("${pba.account.liberata.check.enabled}")
    private boolean pbaAccountLiberataCheckEnabled;


    private HashMap<String, String> pbaAccounts = new HashMap<>();


    @Test
    public void verifyGetFeeLoopUpTest() {

        validatePostSuccess(feeLookup, "fee-lookup.json");
    }

    @Ignore
    @Test
    public void verifyPBAValidationTest() {

        validatePostSuccessForPBAValidation(pbaValidate);
    }

    @Ignore
    @Test
    public void verifyPBAPaymentSuccessTest() {

        validatePostSuccessForPBAPayment(pbaPayment);
    }

    @Ignore
    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment);

    }

    @Ignore
    @Test
    public void verifyPBAConfirmationForHWF() {
        validatePBAConfirmationForHWF();

    }

    @Test
    public void verifyPBAConfirmationForPBAPayment() {

        validatePBAConfirmationForPBAPayment();
    }

    private void validatePostSuccess(String url, String jsonFileName) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post(url)
                .then()
                .assertThat().statusCode(200);
    }


    public void validatePostSuccessForPBAValidation(String url) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile("pba-validate.json"))
                .when().post(pbaValidate)
                .then()
                .assertThat().statusCode(200);
    }

    private void validatePBAConfirmationForHWF() {

        Response response = getPBAPaymentResponse(pbaConfirmation, "hwfPayment.json");

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Process the application for help with fees"));

    }

    private void validatePBAConfirmationForPBAPayment() {

        Response response = getPBAPaymentResponse(pbaConfirmation, "pba-payment.json");

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Your application will be issued by Court staff and referred to a Judge"));

    }

    private void validateFailurePBAPayment(String url) {

        Response response = getPBAPaymentResponse(url, "FailurePaymentRequestPayload.json");

        int statusCode = response.getStatusCode();

        System.out.println("ValidateFailurePBAPayment" + "status Code : "
                + statusCode + response.getBody().prettyPrint());

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(statusCode, 200);

        if (pbaAccountLiberataCheckEnabled) {
            List<String> errors = jsonPathEvaluator.get("errors");
            assertTrue(errors.get(0).contains("Account information could not be found"));
        }

    }

    private void validatePostSuccessForPBAPayment(String url) {
        Response response = getPBAPaymentResponse(url, "SuccessPaymentRequestPayload.json");

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath().setRoot("data");

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("state").toString()
                .equalsIgnoreCase("applicationSubmitted"));
    }


    private Response getPBAPaymentResponse(String url, String payload) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(payload))
                .when().post(url)
                .andReturn();
    }

}
