package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class PBAPaymentConfirmationService {
    public static final String PBA_PAYMENT_CONFIRMATION_MARKDOWN = "/markdown/pba-payment-confirmation.md";

    public String paymentConfirmationMarkdown() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(PBA_PAYMENT_CONFIRMATION_MARKDOWN)) {
            return IOUtils.toString(inputStream, "UTF-8");
        }
    }
}
