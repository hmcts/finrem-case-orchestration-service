package uk.gov.hmcts.reform.finrem.functional.payments;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class PaymentServiceTests extends IntegrationTestBase {

    private static String FEE_LOOKUP = "/payments/fee-lookup";
    private static String PBA_VALIDATE = "/payments/pba-validate/";
    private static String PBA_PAYMENT = "/payments/pba-payment";


    @Value("${payment_api_url }")
    private String pbaValidationUrl;

    @Value("${idam.s2s-auth.microservice}")
    private String microservice;

    @Value("${idam.s2s-auth.secret}")
    private String authClientSecret;


    @Value("${pdf_access_key}")
    private String pdfAccessKey;


    //@Test
    public void verifyGetFeeLoopUpTest() {


        validatePostSuccess(FEE_LOOKUP);

    }

    //@Test
    public void verifyPBAValidationTest() {
        validatePostSuccessForPBAValidation(PBA_VALIDATE);
    }

    //@Test
    public void verifyPBAPaymentTest() {

        validatePostSuccessForPBAPayment(PBA_PAYMENT);

    }


    private void validatePostSuccess(String url) {
        IntegrationTestBase.setPaymentServiceUrlUrlAsBaseUri();
        System.out.println("Fee LookUp : " + pbaValidationUrl + url);

        SerenityRest.given()
                .relaxedHTTPSValidation()
                .when().get(pbaValidationUrl + url)
                .then()
                .assertThat().statusCode(200);
    }



    public void validatePostSuccessForPBAValidation(String url) {

        IntegrationTestBase.setPaymentServiceUrlUrlAsBaseUri();
        System.out.println("PBA Validation : " + pbaValidationUrl + url);
        System.out.println("Testing vaul access " + pdfAccessKey);
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .param("pbaNumber", "PBA222")
                .when().get(pbaValidationUrl + url)
                .then()
                .assertThat().statusCode(200);
    }


    public void validatePostSuccessForPBAPayment(String url) {

        IntegrationTestBase.setPaymentServiceUrlUrlAsBaseUri();
        System.out.println("PBA Payment : " + pbaValidationUrl + url);
        SerenityRest.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeader())
                .body(utils.getJsonFromFile("paymentRequestPayload.json"))
                .when().post(pbaValidationUrl + url)
                .then()
                .assertThat().statusCode(200);
    }

}
