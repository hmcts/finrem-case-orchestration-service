package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentConfirmationService;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HELP_WITH_FEES_QUESTION;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class PaymentConfirmationController extends BaseController {

    private final PaymentConfirmationService paymentConfirmationService;
    private final CaseDataService caseDataService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/payment-confirmation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles PBA Payments Confirmation")
    public ResponseEntity<SubmittedCallbackResponse> paymentConfirmation(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for PBA confirmation for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        return ResponseEntity.ok(SubmittedCallbackResponse.builder()
            .confirmationBody(confirmationBody(caseDetails))
            .build());
    }

    private String confirmationBody(CaseDetails caseDetails) throws IOException {
        boolean isConsentedApplication = caseDataService.isConsentedApplication(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        log.info("Application type isConsentedApplication : {}", isConsentedApplication);

        String confirmationBody;
        boolean helpWithFeeQuestion = Objects.toString(caseData.get(HELP_WITH_FEES_QUESTION)).equalsIgnoreCase("no");
        if (isConsentedApplication) {
            log.info("Consented confirmation page to show");
            confirmationBody = helpWithFeeQuestion ? paymentConfirmationService.consentedPbaPaymentConfirmation()
                : paymentConfirmationService.consentedHwfPaymentConfirmation();
        } else {
            log.info("Contested confirmation page to show");
            confirmationBody = helpWithFeeQuestion ? paymentConfirmationService.contestedPbaPaymentConfirmation()
                : paymentConfirmationService.contestedHwfPaymentConfirmation();
            confirmationBody = addCourtContactInformation(confirmationBody, caseDetails);
        }

        return confirmationBody;
    }

    private String addCourtContactInformation(String confirmationBody, CaseDetails caseDetails) {
        Map<String, Object> courtDetails = CaseHearingFunctions.buildFrcCourtDetails(caseDetails.getData());

        return String.format(confirmationBody,
            courtDetails.get(COURT_DETAILS_NAME_KEY),
            courtDetails.get(COURT_DETAILS_ADDRESS_KEY),
            courtDetails.get(COURT_DETAILS_EMAIL_KEY),
            courtDetails.get(COURT_DETAILS_PHONE_KEY)
        );
    }
}
