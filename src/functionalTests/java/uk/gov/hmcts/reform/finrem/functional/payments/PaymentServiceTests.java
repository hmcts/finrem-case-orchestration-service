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
    private String hwfPayment = "HWF";
    private String Pbapayment = "PBA";



    @Test
    public void verifyGetFeeLoopUpTestConsented() {

         ValidatePostSuccess(feeLookup, "fee-lookup.json",consentedDir);
         Response response = getResponse(feeLookup, "fee-lookup.json",consentedDir);
         JsonPath jsonPathEvaluator = response.jsonPath();

        assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode").toString().equalsIgnoreCase("FEE0640"));
        assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode").toString().equalsIgnoreCase("1000"));

    }


    @Test
    public void verifyGetFeeLoopUpTestContested() {

        ValidatePostSuccess(feeLookup, "fee-lookup.json",contestedDir);
        Response response = getResponse(feeLookup, "fee-lookup.json",contestedDir);
        JsonPath jsonPathEvaluator = response.jsonPath();

        assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode").toString().equalsIgnoreCase("FEE0640"));
        assertTrue(jsonPathEvaluator.get("data.orderSummary.Fees[0].value.FeeCode").toString().equalsIgnoreCase("25500"));

    }


    @Test
    public void verifyPBAValidationTest() {

        ValidatePostSuccess(pbaValidate, "pba-validate.json" ,consentedDir );
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
        validatePBAConfirmation(pbaConfirmation, "hwfPayment.json", consentedDir,hwfPayment);

    }

    @Test
    public void verifyPBAConfirmationForPBAPaymentConsented() {

        validatePBAConfirmation(pbaConfirmation, "pba-payment.json", consentedDir, pbaPayment);
    }

    @Test
    public void verifyPBAConfirmationForHWFContested() {
        validatePBAConfirmation(pbaConfirmation, "hwfPayment.json", contestedDir,hwfPayment);

    }


    @Test
    public void verifyPBAConfirmationForPBAPaymentContested() {

        validatePBAConfirmation(pbaConfirmation, "pba-payment.json", contestedDir,pbaPayment);
    }


    private void validatePBAConfirmation(String url, String fileName, String journeyType, String paymentType) {

        Response response = getResponse(url, fileName , journeyType);
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath();
        assertEquals(statusCode, 200);

        if ( paymentType == "HWF") {

            assertTrue(jsonPathEvaluator.get("confirmation_body")
                    .toString().contains("Process the application for help with fees"));
        } else
            {
                assertTrue(jsonPathEvaluator.get("confirmation_body")
                        .toString().contains("Your application will be issued by Court staff and referred to a Judge"));
            }

    }


    private void validateFailurePBAPayment(String url, String fileName, String journeyType) {

        Response response = getResponse(url, fileName , journeyType);
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

    private void validatePostSuccessForPBAPayment(String url, String fileName, String journeyType) {

        Response response = getResponse(url, fileName, journeyType);
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath().setRoot("data");

        assertEquals(statusCode, 200);

        if ( journeyType == consentedDir) {
            assertTrue(jsonPathEvaluator.get("state").toString()
                    .equalsIgnoreCase("applicationSubmitted"));
        } else {
            assertTrue(jsonPathEvaluator.get("state").toString()
                    .equalsIgnoreCase("gateKeepingAndAllocation"));
        }
    }

    private void ValidatePostSuccess(String url, String filename, String journeyType)
    {
        Response response = getResponse(url, filename, journeyType);
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200);

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
