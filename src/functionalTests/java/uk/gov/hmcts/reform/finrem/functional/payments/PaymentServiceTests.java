package uk.gov.hmcts.reform.finrem.functional.payments;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
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
    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";


    @Test
    public void verifyGetFeeLoopUpTestConsented() {

        validateFeeLookUpPayment(feeLookup, "fee-lookup_consented.json",consentedDir);
    }


    @Test
    public void verifyGetFeeLoopUpTestContested() {

        validateFeeLookUpPayment(feeLookup, "fee-lookup_contested.json",contestedDir);
    }


    @Test
    public void verifyPBAValidationTest() {

        validatePostSuccessForPBAValidation(pbaValidate, "pba-validate.json" ,consentedDir );
    }

    @Test
    public void verifyPBAPaymentSuccessTestConsented() {

        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload.json" , consentedDir);
    }

    @Test
    public void verifyPBAPaymentSuccessTestContested() {

        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload.json" , contestedDir);
    }

    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment, "FailurePaymentRequestPayload.json" , consentedDir);

    }

    @Test
    public void verifyPBAConfirmationForHWFConsented() {
        validatePBAConfirmationForHWF(pbaConfirmation, "hwfPayment.json", consentedDir);

    }

    @Test
    public void verifyPBAConfirmationForPBAPaymentConsented() {

        validatePBAConfirmationForPBAPayment(pbaConfirmation, "pba-payment.json", consentedDir);
    }

    @Test
    public void verifyPBAConfirmationForHWFContested() {
        validatePBAConfirmationForHWF(pbaConfirmation, "hwfPayment.json", contestedDir);

    }

    @Test
    public void verifyPBAConfirmationForPBAPaymentContested() {

        validatePBAConfirmationForPBAPayment(pbaConfirmation, "pba-payment.json", contestedDir);
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

    private void validatePostSuccess(String url, String filename, String journeyType) {
        Response response = getResponse(url, filename, journeyType);
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200);

    }

    public void validatePostSuccessForPBAValidation(String url, String fileName , String journeyType) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile("pba-validate.json"))
                .when().post(pbaValidate)
                .then()
                .assertThat().statusCode(200);
    }

    private void validatePBAConfirmationForHWF(String url, String fileName, String journeyType) {

        Response response = getPBAPaymentResponse(url, fileName , journeyType);

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Process the application for help with fees"));

    }

    private void validatePBAConfirmationForPBAPayment(String url, String fileName, String journeyType) {

        Response response = getPBAPaymentResponse(url, fileName, journeyType);

        int statusCode = response.getStatusCode();


        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Your application will be issued by Court staff and referred to a Judge"));

    }

    private void validateFailurePBAPayment(String url, String fileName, String journeyType) {

        Response response = getPBAPaymentResponse(url, fileName , journeyType);

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(statusCode, 200);

        if (pbaAccountLiberataCheckEnabled) {
            List<String> errors = jsonPathEvaluator.get("errors");
            assertTrue(errors.get(0).contains("Account information could not be found"));
        }

    }

    private void validatePostSuccessForPBAPayment(String url, String fileName, String journeyType) {
        Response response = getPBAPaymentResponse(url, fileName, journeyType);

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath().setRoot("data");

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("state").toString()
                    .equalsIgnoreCase("applicationSubmitted"));

    }


    private Response getPBAPaymentResponse(String url, String filename, String journeyType) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename))
                .when().post(url)
                .andReturn();
    }

    private void validateFeeLookUpPayment(String url, String fileName, String journeyType) {
        validatePostSuccess(url, fileName ,journeyType);
        Response response = getResponse(url, fileName ,journeyType);
        JsonPath jsonPathEvaluator = response.jsonPath();


        if (journeyType == consentedDir) {
            assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeAmount")
                    .toString().equalsIgnoreCase("5000"));
            assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode")
                    .toString().equalsIgnoreCase("FEE0228"));

        } else {
            assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeAmount")
                    .toString().equalsIgnoreCase("25500"));
            assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode")
                    .toString().equalsIgnoreCase("FEE0229"));

        }

    }



    private Response getResponse(String url, String filename, String journeyType) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename , journeyType))
                .when().post(url)
                .andReturn();
    }

}
