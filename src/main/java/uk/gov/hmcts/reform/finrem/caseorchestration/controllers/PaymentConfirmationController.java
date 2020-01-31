package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentConfirmationService;

import java.io.IOException;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class PaymentConfirmationController implements BaseController {
    private final PaymentConfirmationService paymentConfirmationService;

    @PostMapping(path = "/payment-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<SubmittedCallbackResponse> paymentConfirmation(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) throws IOException {
        log.info("Received request for PBA confirmation. Auth token: {}, Case request : {}", authToken,
            callbackRequest);

        validateCaseData(callbackRequest);

        Map<String,Object> caseData = callbackRequest.getCaseDetails().getData();

        String confirmationBody = confirmationBody(caseData);
        log.info("confirmationBody : {}", confirmationBody);

        SubmittedCallbackResponse callbackResponse = SubmittedCallbackResponse.builder()
                .confirmationBody(confirmationBody)
                .build();

        return ResponseEntity.ok(callbackResponse);
    }

    private String confirmationBody(Map<String, Object> caseData) throws IOException {
        boolean isConsentedApplication = isConsentedApplication(caseData);
        log.info("Application type isConsentedApplication : {}", isConsentedApplication);

        String confirmationBody;
        if (isConsentedApplication) {
            log.info("Consented confirmation page to show");
            confirmationBody = isPBAPayment(caseData) ?  paymentConfirmationService.consentedPbaPaymentConfirmation()
                    : paymentConfirmationService.consentedHwfPaymentConfirmation();
        } else {
            log.info("Contested confirmation page to show");
            confirmationBody = isPBAPayment(caseData) ?  paymentConfirmationService.contestedPbaPaymentConfirmation()
                    : paymentConfirmationService.contestedHwfPaymentConfirmation();
        }
        return confirmationBody;
    }

}
