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
    public static final String PBA_PAYMENT_CONFIRMATION_MARKDOWN = "/markdown/pba-payment-confirmation.md";
    public static final String HWF_PAYMENT_CONFIRMATION_MARKDOWN = "/markdown/hwf-payment-confirmation.md";

    public String pbaPaymentConfirmationMarkdown() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(PBA_PAYMENT_CONFIRMATION_MARKDOWN)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    public String hwfPaymentConfirmationMarkdown() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(HWF_PAYMENT_CONFIRMATION_MARKDOWN)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }
}
