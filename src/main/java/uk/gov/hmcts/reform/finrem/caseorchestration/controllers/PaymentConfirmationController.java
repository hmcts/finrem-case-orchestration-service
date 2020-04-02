package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentConfirmationService;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PaymentConfirmationController implements BaseController {
    private final PaymentConfirmationService paymentConfirmationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/payment-confirmation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles PBA Payments Confirmation")
    public ResponseEntity<SubmittedCallbackResponse> paymentConfirmation(
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for PBA confirmation for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);
        Map<String,Object> caseData = caseDetails.getData();

        SubmittedCallbackResponse callbackResponse = SubmittedCallbackResponse.builder()
            .confirmationBody(confirmationBody(caseData))
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
