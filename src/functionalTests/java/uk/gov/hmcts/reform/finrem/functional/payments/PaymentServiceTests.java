package uk.gov.hmcts.reform.finrem.functional.payments;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SerenityRunner.class)
public class PaymentServiceTests extends IntegrationTestBase {

    @Value("${cos.payment.fee.lookup.api}")
    private String feeLookup;

    @Value("${cos.payment.pba.validate.api}")
    private String pbaValidate;

    @Value("${cos.payment.pba.api}")
    private String pbaPayment;

    @Value("${cos.payment.pba.confirmation.api}")
    private String pbaConfirmation;

    @Value("${pba.account.liberata.check.enabled}")
    private boolean pbaAccountLiberataCheckEnabled;

    private String contestedDir = "/json/contested/";
    private String consentedDir = "/json/consented/";
    private String dataPath = "data";
    private String feesPath = "data.orderSummary.Fees[0].value";
    private String hwf = "HWF";
    private String pba = "PBA";

    @Test
    public void verifyPBAPaymentSuccessTestContested() {
        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Contested.json", contestedDir);
    }

    @Test
    public void verifyGetFeeLoopUpTestConsented() {
        validateFeeLookUpPayment(feeLookup, "fee-lookup_consented.json", consentedDir);
    }

    @Test
    public void verifyGetFeeLoopUpTestContested() {
        validateFeeLookUpPayment(feeLookup, "fee-lookup_contested.json", contestedDir);
    }

    @Test
    public void verifyPBAValidationTest() {
        utils.validatePostSuccess(pbaValidate, "pba-validate1.json", consentedDir);
    }

    @Test
    public void verifyPBAPaymentSuccessTestConsented() {
        validatePostSuccessForPBAPayment(pbaPayment, "SuccessPaymentRequestPayload_Consented.json", consentedDir);
    }

    @Test
    public void verifyPBAPaymentFailureTest() {
        validateFailurePBAPayment(pbaPayment, "FailurePaymentRequestPayload.json", consentedDir);
    }

    @Test
    public void verifyPaymentConfirmationMessageForHwfConsented() {
        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", consentedDir, hwf);
    }

    @Test
    public void verifyPaymentConfirmationMessageForPBAPaymentConsented() {
        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment.json", consentedDir, pba);
    }

    @Test
    public void verifyPBAConfirmationMessageForHwfContested() {
        validatePaymentConfirmationMessage(pbaConfirmation, "hwfPayment.json", contestedDir, hwf);
    }

    @Test
    public void verifyPBAConfirmationMessageForPBAPaymentContested() {
        validatePaymentConfirmationMessage(pbaConfirmation, "pba-payment_contested.json", contestedDir, pba);
    }

    /**
     * Verify a "duplicate payment" error is received when sending a fee with the same fee code more than once within
     * 2 minutes.
     */
    @Test
    public void verifyDuplicatePaymentReturnsErrorWithin2MinutesForContested() {
        String filename = "SuccessPaymentRequestPayload_Contested_Duplicate.json";
        utils.validatePostSuccess(pbaPayment, filename, contestedDir);
        assertThat(utils.getResponse(pbaPayment, filename, contestedDir).jsonPath().get("errors[0]"), is("duplicate payment"));
    }

    private void validatePaymentConfirmationMessage(String url, String fileName,
                                                    String journeyType, String paymentType) {
        if (paymentType.equals(pba)) {
            if (journeyType.equals(consentedDir)) {
                assertTrue(utils.getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                    .toString().contains("Your application will be issued by Court staff and referred to a Judge"));
            } else {
                assertTrue(utils.getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                    .toString().contains("The application will be sent to the Judge for gatekeeping"));
            }
        } else if (paymentType.equals(hwf)) {
            if (journeyType.equals(consentedDir)) {
                assertTrue(utils.getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                    .toString().contains("Process the application for help with fees"));
            } else {
                assertTrue(utils.getResponseData(url, fileName, journeyType, "").get("confirmation_body")
                    .toString().contains("process the application for help with fees"));
            }
        }
    }

    private void validateFailurePBAPayment(String url, String fileName, String journeyType) {
        if (pbaAccountLiberataCheckEnabled) {
            List<String> errors = utils.getResponseData(url, fileName, journeyType, "").get("errors");
            assertTrue(errors.get(0).contains("Account information could not be found"));
        }
    }

    private void validatePostSuccessForPBAPayment(String url, String fileName, String journeyType) {
        utils.getResponseData(url, fileName, journeyType, dataPath);
    }

    private void validateFeeLookUpPayment(String url, String fileName, String journeyType) {
        if (journeyType.equals(consentedDir)) {
            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeAmount")
                .toString().equalsIgnoreCase("5300"));

            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeCode")
                .toString().equalsIgnoreCase("FEE0228"));
        } else {
            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeAmount")
                .toString().equalsIgnoreCase("27500"));

            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeCode")
                .toString().equalsIgnoreCase("FEE0229"));
        }
    }
}
