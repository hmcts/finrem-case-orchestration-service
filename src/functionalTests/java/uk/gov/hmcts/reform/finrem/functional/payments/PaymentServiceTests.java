package uk.gov.hmcts.reform.finrem.functional.payments;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.finrem.functional.idam.IdamUtils;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class PaymentServiceTests extends IntegrationTestBase {

    @Autowired
    private IdamUtils idamUtils;

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


    private HashMap<String, String> pbaAccounts = new HashMap<>();


    @Test
    public void verifyGetFeeLoopUpTest() {

        validatePostSuccess(feeLookup , "fee-lookup.json");
    }

    @Test
    public void verifyPBAValidationTest() {

        validatePostSuccessForPBAValidation(pbaValidate);
    }

    @Test
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
                .when().post(pbaValidate)
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

        assertTrue(errors.get(0).contains("Access Denied"));

    }

    private void validatePostSuccessForPBAPayment(String url) {
        System.out.println("PBA Payment : " + url);

        Response response = getPBAPaymentResponse(url, "SuccessPaymentRequestPayload.json");



        String token = idamUtils.generateUserTokenWithNoRoles(idamUserName,idamUserPassword);

        System.out.println("Validate Post Payment data:" + response.jsonPath().prettyPrint());

        System.out.println("Print idam secret  :" + idamSecret );

        System.out.println("Authorization token :" + token );

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers("Authorization","Bearer " + token)
                .contentType("application/json")
                .body(utils.getJsonFromFile("SuccessPaymentRequestPayload.json"))
                .when().post(url).then().assertThat().statusCode(200);

        //int statusCode = response.getStatusCode();

        //JsonPath jsonPathEvaluator = response.jsonPath().setRoot("data");

        //System.out.println("Validate Post Payment data:" + response.jsonPath().get("data"));

        //System.out.println("Validate Post Payment state:" + jsonPathEvaluator.get("state"));

        //assertEquals(statusCode, 200);
        //assertTrue(jsonPathEvaluator.get("data.state").toString()
        //        .equalsIgnoreCase("applicationSubmitted"));
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


}
