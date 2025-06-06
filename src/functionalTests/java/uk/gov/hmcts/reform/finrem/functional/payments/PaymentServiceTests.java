package uk.gov.hmcts.reform.finrem.functional.payments;

import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.finrem.functional.IntegrationTestBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
            String feeAmount = utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeAmount").toString();
            assertThat(feeAmount).matches("\\d+");

            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeCode")
                .toString().equalsIgnoreCase("FEE0228"));
        } else {
            String feeAmount = utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeAmount").toString();
            assertThat(feeAmount).matches("\\d+");

            assertTrue(utils.getResponseData(url, fileName, journeyType, feesPath).get("FeeCode")
                .toString().equalsIgnoreCase("FEE0229"));
        }
    }
}
