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
public class PBAPaymentConfirmationService {
    public static final String PBA_PAYMENT_CONFIRMATION_MARKDOWN = "/markdown/pba-payment-confirmation.md";

    public String paymentConfirmationMarkdown() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(PBA_PAYMENT_CONFIRMATION_MARKDOWN)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }
}
