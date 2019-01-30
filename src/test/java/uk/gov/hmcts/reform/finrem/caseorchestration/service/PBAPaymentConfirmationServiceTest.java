package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class PBAPaymentConfirmationServiceTest extends BaseServiceTest {

    @Autowired
    private PBAPaymentConfirmationService pbaPaymentConfirmationService;

    @Test
    public void verifyPaymentConfirmationMarkdown() throws Exception {
        assertThat(pbaPaymentConfirmationService.paymentConfirmationMarkdown(),
                containsString("# Application Complete"));
        assertThat(pbaPaymentConfirmationService.paymentConfirmationMarkdown(),
                containsString("Your application has now been submitted to the Court."));
    }


}