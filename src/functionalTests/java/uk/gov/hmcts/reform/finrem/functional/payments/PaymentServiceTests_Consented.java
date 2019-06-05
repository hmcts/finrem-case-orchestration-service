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
public class PaymentServiceTests_Consented extends IntegrationTestBase {

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
    private String feesPath = "data.orderSummary.Fees[0].value";
    private String hwf = "HWF";
    private String pba = "PBA";


    @Test
    public void verifyPBAPaymentSuccessTestContested() throws InterruptedException {

        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Contested.json" , contestedDir);
    }


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
    public void verifyPBAPaymentSuccessTestConsented() throws InterruptedException {

        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Consented.json" , consentedDir);
    }


    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment, "FailurePaymentRequestPayload.json" , consentedDir);

    }

    @Test
    public void verifyPaymentConfirmationMessageForHWFConsented() throws InterruptedException {

        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", consentedDir, hwf);


    }

    @Test
    public void verifyPaymentConfirmationMessageForPBAPaymentConsented() throws InterruptedException {


        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment.json", consentedDir, pba);

    }


    @Test
    public void verifyPBAConfirmationMessageForHWFContested() {
        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", contestedDir, hwf);

    }

    @Test
    public void verifyPBAConfirmationMessageForPBAPaymentContested() {

        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment_contested.json", contestedDir, pba);
    }


    @Test
    public void verifyDuplicatePaymentReturnsErrorWithin2MinutesForContested() throws InterruptedException{

        validateDuplicatePayment(pbaPayment, "SuccessPaymentRequestPayload_Contested_Duplicate.json" , contestedDir);

    }


    private void validatePostSuccess(String url, String filename, String journeyType) {
        int statusCode = getResponse(url, filename, journeyType).getStatusCode();
        assertEquals(statusCode, 200);

    }

    private void validateDuplicatePayment(String url, String filename, String journeyType) throws InterruptedException{

        validatePostSuccess(url, filename, journeyType);
       assertEquals("duplicate payment", getResponse(url, filename, journeyType).jsonPath().get("errors[0]"));
       Thread.sleep(120000);
       validatePostSuccess(url, filename, journeyType);
    }

    private void validatePaymentConfirmationMessage(String url, String fileName,
                                                    String journeyType, String paymentType) {

        if (paymentType == pba) {
            if (journeyType == consentedDir) {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("Your application will be issued by Court staff and referred to a Judge"));
            } else {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("The application will be sent to the Judge for gatekeeping"));

            }
        } else if (paymentType == hwf) {

            if (journeyType == consentedDir) {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("Process the application for help with fees"));
            } else {
                assertTrue(getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                        .toString().contains("process the application for help with fees"));

            }
        }
    }


    private void validateFailurePBAPayment(String url, String fileName, String journeyType) {

        if (pbaAccountLiberataCheckEnabled) {

            List<String> errors = getResponseData(url, fileName, journeyType,"").get("errors");

            assertTrue(errors.get(0).contains("Account information could not be found"));
        }

    }

    private void validatePostSuccessForPBAPayment(String url, String fileName, String journeyType) {

        assertTrue(getResponseData(url, fileName, journeyType,dataPath).get("state").toString()
                    .equalsIgnoreCase("applicationSubmitted"));
    }

    private void validateFeeLookUpPayment(String url, String fileName, String journeyType) {


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



    private JsonPath getResponseData(String url, String filename, String journeyType, String dataPath) {
        System.out.println("url : " + url);
        System.out.println("request :" + utils.getJsonFromFile(filename, journeyType));
        Response response = SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename, journeyType))
                .when().post(url)
                .andReturn();
        System.out.println("response " + response.prettyPrint());
        jsonPathEvaluator = response.jsonPath().setRoot(dataPath);
        int statusCode = response.getStatusCode();
        assertEquals(statusCode, 200);
        return jsonPathEvaluator;
    }

    public Response getResponse(String url, String filename, String journeyType)
    {
       return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(filename , journeyType))
                .when().post(url).andReturn();
    }

}
