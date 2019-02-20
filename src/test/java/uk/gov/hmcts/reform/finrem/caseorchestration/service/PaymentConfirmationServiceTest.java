package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class PaymentConfirmationServiceTest extends BaseServiceTest {

    @Autowired
    private PaymentConfirmationService paymentConfirmationService;

    @Test
    public void verifyPBAPaymentConfirmationMarkdown() throws Exception {
        assertThat(paymentConfirmationService.pbaPaymentConfirmationMarkdown(),
                containsString("You will now be directed to the case file where you can monitor the progress"
                        + " of your application via the ‘history’ tab. Next:"));
        assertThat(paymentConfirmationService.pbaPaymentConfirmationMarkdown(),
                containsString("* Your application will be issued by Court staff and referred to a Judge"));
        assertThat(paymentConfirmationService.pbaPaymentConfirmationMarkdown(),
                containsString("* The Judge will consider your application and make an order"));
    }

    @Test
    public void verifyHWFPaymentConfirmationMarkdown() throws Exception {
        assertThat(paymentConfirmationService.hwfPaymentConfirmationMarkdown(),
                containsString("The application will be received by Court staff who will:"));
        assertThat(paymentConfirmationService.hwfPaymentConfirmationMarkdown(),
                containsString("* Check the application"));
        assertThat(paymentConfirmationService.hwfPaymentConfirmationMarkdown(),
                containsString("* Process the application for help with fees"));
    }

}