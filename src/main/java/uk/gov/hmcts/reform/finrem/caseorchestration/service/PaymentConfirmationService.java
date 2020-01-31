package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationService {
    private static final String CONSENTED_PBA_PAYMENT_CONFIRMATION = "/markdown/consented-pba-payment-confirmation.md";
    private static final String CONSENTED_HWF_PAYMENT_CONFIRMATION = "/markdown/consented-hwf-payment-confirmation.md";

    private static final String CONTESTED_PBA_PAYMENT_CONFIRMATION = "/markdown/contested-pba-payment-confirmation.md";
    private static final String CONTESTED_HWF_PAYMENT_CONFIRMATION = "/markdown/contested-hwf-payment-confirmation.md";

    public String consentedPbaPaymentConfirmation() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(CONSENTED_PBA_PAYMENT_CONFIRMATION)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    public String consentedHwfPaymentConfirmation() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(CONSENTED_HWF_PAYMENT_CONFIRMATION)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    public String contestedPbaPaymentConfirmation() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(CONTESTED_PBA_PAYMENT_CONFIRMATION)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    public String contestedHwfPaymentConfirmation() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(CONTESTED_HWF_PAYMENT_CONFIRMATION)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }
}
