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
    public void verifyConsentedPBAPaymentConfirmation() throws Exception {
        String confirmation = paymentConfirmationService.consentedPbaPaymentConfirmation();

        assertThat(confirmation, containsString("You will now be directed to the case file where you can "
                + "monitor the progress of your application via the ‘history’ tab. Next:"));
        assertThat(confirmation, containsString("* Your application will be issued by Court staff and referred to a Judge"));
        assertThat(confirmation, containsString("* The Judge will consider your application and make an order"));
    }

    @Test
    public void verifyConsentedHWFPaymentConfirmation() throws Exception {
        String confirmation = paymentConfirmationService.consentedHwfPaymentConfirmation();

        assertThat(confirmation, containsString("The application will be received by Court staff who will:"));
        assertThat(confirmation, containsString("* Check the application"));
        assertThat(confirmation, containsString("* Process the application for help with fees"));
    }

    @Test
    public void verifyContestedPBAPaymentConfirmation() throws Exception {
        String confirmation = paymentConfirmationService.contestedPbaPaymentConfirmation();

        assertThat(confirmation, containsString("You will receive a notification when the Notice of First "
                + "Appointment is available. If there are any issues, the Court will contact you."));
        assertThat(confirmation, containsString("* The application will be sent to the Judge for gatekeeping "
                        + "(where applicable)"));
        assertThat(confirmation, containsString("* A First Appointment hearing will be set"));
    }

    @Test
    public void verifyContestedHWFPaymentConfirmation() throws Exception {
        String confirmation = paymentConfirmationService.contestedHwfPaymentConfirmation();

        assertThat(confirmation, containsString("You will now be directed to the case file where you can "
                + "monitor the progress of your application via the ‘history’ tab."));
        assertThat(confirmation, containsString("The application will be received by Court staff who will "
                + "process the application for help with fees."));
        assertThat(confirmation, containsString("You will receive a notification via email confirming the "
                + "outcome of the help with fees application."));
    }
}