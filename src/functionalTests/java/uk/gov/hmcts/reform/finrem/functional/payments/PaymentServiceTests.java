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
    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";
    private JsonPath jsonPathEvaluator;
    private String dataPath = "data";
    private String feesPath= "data.orderSummary.Fees[0].value";
    private String hwf = "HWF";
    private String pba = "PBA";



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

        validatePostSuccess(pbaValidate, "pba-validate1.json" ,consentedDir );
    }

    @Test
    public void verifyPBAPaymentSuccessTestConsented() throws InterruptedException{

        //Thread.sleep(120000);
        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Consented.json" , consentedDir);
    }

    @Test
    public void verifyPBAPaymentSuccessTestContested() throws InterruptedException {
        Thread.sleep(120000);
        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Contested.json" , contestedDir);
    }


    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment, "FailurePaymentRequestPayload.json" , consentedDir);

    }

    @Test
    public void verifyPaymentConfirmationMessageForHWFConsented() throws InterruptedException {

        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", consentedDir, hwf);
        //validatePBAConfirmationForHWF(pbaConfirmation, "hwfPayment.json", consentedDir);

    }

    @Test
    public void verifyPaymentConfirmationMessageForPBAPaymentConsented() throws InterruptedException{


        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment.json", consentedDir, pba);
        //validatePBAConfirmationForPBAPayment(pbaConfirmation, "pba-payment.json", consentedDir);
    }


    @Test
    public void verifyPBAConfirmationMessageForHWFContested() {
        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", contestedDir, hwf);
        //validatePBAConfirmationForHWF(pbaConfirmation, "hwfPayment.json", contestedDir);
    }

    @Test
    public void verifyPBAConfirmationMessageForPBAPaymentContested() {

        //validatePBAConfirmationForPBAPayment(pbaConfirmation, "SuccessPaymentRequestPayload_Contested.json", contestedDir);
        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment_contested.json", contestedDir, pba);
    }


    private void validatePostSuccess(String url, String filename, String journeyType) {
        Response response = getResponse(url, filename, journeyType);
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200);

    }

    private void validatePaymentConfirmationMessage(String url, String fileName, String journeyType, String paymentType)
    {

        validatePostSuccess(url, fileName , journeyType);

        if (paymentType== pba) {
            if (journeyType == consentedDir) {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("Your application will be issued by Court staff and referred to a Judge"));
            } else {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("The application will be sent to the Judge for gatekeeping"));

            }
        }
        else if (paymentType == hwf){

            if (journeyType == consentedDir) {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("Process the application for help with fees"));
            } else {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("process the application for help with fees"));

            }
        }
    }


    private void validatePBAConfirmationForHWF(String url, String fileName, String journeyType) {

        validatePostSuccess(url, fileName , journeyType);

        assertTrue(getResponseData(url, fileName, journeyType,"").get("confirmation_body")
                .toString().contains("process the application for help with fees"));

    }

    private void validatePBAConfirmationForPBAPayment(String url, String fileName, String journeyType) {

        validatePostSuccess(url, fileName , journeyType);

        if (journeyType == consentedDir) {
            assertTrue(getResponseData(url, fileName, journeyType,"").get("confirmation_body")
                    .toString().contains("Your application will be issued by Court staff and referred to a Judge"));
        }
        else{
            assertTrue(getResponseData(url, fileName, journeyType,"").get("confirmation_body")
                    .toString().contains("The application will be sent to the Judge for gatekeeping"));

        }

    }

    private void validateFailurePBAPayment(String url, String fileName, String journeyType) {

        validatePostSuccess(url, fileName , journeyType);

        if (pbaAccountLiberataCheckEnabled) {

            List<String> errors = getResponseData(url, fileName, journeyType,"").get("errors");

            assertTrue(errors.get(0).contains("Account information could not be found"));
        }

    }

    private void validatePostSuccessForPBAPayment(String url, String fileName, String journeyType) {

        validatePostSuccess(url, fileName , journeyType);
        assertTrue(getResponseData(url, fileName, journeyType,dataPath).get("state").toString()
                    .equalsIgnoreCase("applicationSubmitted"));
    }


    private Response getPBAPaymentResponse(String url, String filename, String journeyType) {

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename, journeyType))
                .when().post(url)
                .andReturn();
    }

    private void validateFeeLookUpPayment(String url, String fileName, String journeyType) {
        validatePostSuccess(url, fileName ,journeyType);

        if (journeyType == consentedDir) {
            assertTrue(getResponseData(url, fileName, journeyType,feesPath).get("FeeAmount")
                    .toString().equalsIgnoreCase("5000"));

            assertTrue(getResponseData(url, fileName, journeyType,feesPath).get("FeeCode")
                    .toString().equalsIgnoreCase("FEE0228"));

        } else {
            assertTrue(getResponseData(url, fileName, journeyType,feesPath).get("FeeAmount")
                    .toString().equalsIgnoreCase("25500"));

            assertTrue(getResponseData(url, fileName, journeyType,feesPath).get("FeeCode")
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

    private JsonPath getResponseData(String url, String filename, String journeyType, String dataPath) {
        Response response= SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename , journeyType))
                .when().post(url)
                .andReturn();
            System.out.println("res" + response.prettyPrint());
             jsonPathEvaluator = response.jsonPath().setRoot(dataPath);
            return jsonPathEvaluator;
    }

}
