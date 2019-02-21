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

    private HashMap<String, String> pbaAccounts = new HashMap<>();



    @Test
    public void verifyPBAAccountStatus() {
        pbaAccounts.put(pbaAccountActive, "Active");
        pbaAccounts.put(pbaAccountInActive, "Inactive");

        validatePBAAccountNumber(pbaValidate, pbaAccounts);

    }


    @Test
    public void verifyGetFeeLoopUpTest() {

        validatePostSuccess(feeLookup , "fee-lookup.json");
    }

    @Test
    public void verifyPBAValidationTest() {
        validatePostSuccessForPBAValidation(pbaValidate);
    }

    //@Test
    public void verifyPBAPaymentSuccessTest() {
        validatePostSuccessForPBAPayment(pbaPayment);

    }

    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment);

    }

    @Test
    public void verifyPBAConfirmationForHWF() {
        validatePBAConfirmationForHWF();

    }

    @Test
    public void verifyPBAConfirmationForPBAPayment() {

        validatePBAConfirmationForPBAPayment();
    }

    private void validatePostSuccess(String url, String jsonFileName) {

        System.out.println("Fee LookUp : " + url);

        System.out.println("username :" + idamUserName
                + "     password :" + idamUserPassword);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .contentType("application/json")
                .body(utils.getJsonFromFile(jsonFileName))
                .when().post( url)
                .then()
                .assertThat().statusCode(200);
    }


    public void validatePostSuccessForPBAValidation(String url) {

        System.out.println("PBA Validation : " + url);
        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");


        System.out.println("username :" + idamUserName
                + "     password :" + idamUserPassword);

        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile("pba-validate.json"))
                .when().get(pbaValidate + pbaAccountActive)
                .then()
                .assertThat().statusCode(200);
    }

    private void validatePBAConfirmationForHWF() {

        Response response = getPBAPaymentResponse(pbaConfirmation,"hwfPayment.json");

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Process the application for help with fees"));

    }

    private void validatePBAConfirmationForPBAPayment() {

        Response response = getPBAPaymentResponse(pbaConfirmation,"pba-payment.json");

        int statusCode = response.getStatusCode();

        JsonPath jsonPathEvaluator = response.jsonPath();

        System.out.println("confirmation_body ===========" + jsonPathEvaluator.get("confirmation_body").toString());

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("confirmation_body")
                .toString().contains("Your application will be issued by Court staff and referred to a Judge"));

    }

    private void validateFailurePBAPayment(String url) {

        System.out.println("PBA Payment : " + url);


        Response response = getPBAPaymentResponse(url,"FailurePaymentRequestPayload.json"  );
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath();

        System.out.println("Payment Failure Information : "
                + "                                     "
                + jsonPathEvaluator.get("errors"));

        List<String> errors = jsonPathEvaluator.get("errors");
        assertEquals(statusCode, 200);

        assertTrue(errors.contains("Account information could not be found"));

    }

    private void validatePostSuccessForPBAPayment(String url) {
        System.out.println("PBA Payment : " + url);

        Response response = getPBAPaymentResponse(url,"SuccessPaymentRequestPayload.json"  );
        int statusCode = response.getStatusCode();
        JsonPath jsonPathEvaluator = response.jsonPath();

        assertEquals(statusCode, 200);

        assertTrue(jsonPathEvaluator.get("state").toString()
                .equalsIgnoreCase("applicationSubmitted"));
        assertPaymentResponse(url,"SuccessPaymentRequestPayload.json"  );
    }

    private void assertPaymentResponse(String url, String payload) {

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(payload))
                .when().post(url).then().assertThat().statusCode(200);

    }

    private Response getPBAPaymentResponse(String url, String payload) {

        System.out.println("PBA Validation : " + url);
        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");


        System.out.println("username :" + idamUserName
                + "     password :" + idamUserPassword);

        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");


        System.out.println("Resource URL payload file  : " + payload);

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .contentType("application/json")
                .body(utils.getJsonFromFile(payload))
                .when().post(url)
                .andReturn();
    }

    private void validatePBAAccountNumber(String url, HashMap<String, String> pbaAccount) {

        System.out.println("PBA Validation : " + url);

        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");


        System.out.println("username :" + idamUserName
                + "     password :" + idamUserPassword);


        System.out.println("===================================================="
                +
                "                                                               "
                +
                "==============================================================="
                +
                "                                                               "
                +
                "================================================================");

        pbaAccount.forEach((account, status) -> {

            Response response = SerenityRest.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeader())
                    .when().post(url + account).andReturn();

            JsonPath jsonPathEvaluator = response.jsonPath();

            System.out.println("get pbaNumber status ====================: "
                    + jsonPathEvaluator.get("pbaNumberValid"));

            if (status.equalsIgnoreCase("Active")) {

                assertTrue(jsonPathEvaluator.get("pbaNumberValid").toString()
                        .equalsIgnoreCase("true"));

            } else if (status.equalsIgnoreCase("Inactive")) {

                assertTrue(jsonPathEvaluator.get("pbaNumberValid").toString()
                        .equalsIgnoreCase("false"));
            }

        });
    }





}
