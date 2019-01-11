package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.Fee;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class FeePaymentController {
    private final FeeService feeService;
    private final PBAValidationService pbaValidationService;
    private final PBAPaymentService pbaPaymentService;

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> feeLookup(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for FEE lookup. Auth token: {}, Case request : {}", authToken, ccdRequest);

        if (!isValidCaseData(ccdRequest)) {
            return new ResponseEntity("Missing case data from CCD request.", HttpStatus.BAD_REQUEST);
        }

        Fee fee = feeService.getApplicationFee();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String amountToPay = String.valueOf(fee.getFeeAmount().multiply(BigDecimal.valueOf(100)).longValue());
        caseData.setAmountToPay(amountToPay);
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    private boolean isValidCaseData(CCDRequest ccdRequest) {
        return ccdRequest != null && ccdRequest.getCaseDetails() != null
                && ccdRequest.getCaseDetails().getCaseData() != null;
    }


    private boolean isPBAPayment(CaseData caseData) {
        return caseData.getHelpWithFeesQuestion() != null && caseData.getHelpWithFeesQuestion().equalsIgnoreCase("no");
    }


    @PostMapping(
            path = "/pba-validate",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> pbaValidate(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for PBA validate. Auth token: {}, Case request : {}", authToken, ccdRequest);

        if (!isValidCaseData(ccdRequest)) {
            return new ResponseEntity("Missing case data from CCD request.", HttpStatus.BAD_REQUEST);
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (isPBAPayment(caseData)) {
            log.info("Validate PBA Number :  {}", caseData.getPbaNumber());
            if (!pbaValidationService.isValidPBA(authToken, caseData.getPbaNumber())) {
                log.info("PBA number is invalid.");
                return ResponseEntity.ok(CCDCallbackResponse.builder()
                        .errors(Arrays.asList("PBA Account Number is not valid, please enter a valid one."))
                        .build());
            }
            log.info("PBA number is valid.");
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().build());
    }



    @PostMapping(
            path = "/pba-payment",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> pbaPayment(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for PBA payment. Auth token: {}, Case request : {}", authToken, ccdRequest);

        if (!isValidCaseData(ccdRequest)) {
            return new ResponseEntity("Missing case data from CCD request.", HttpStatus.BAD_REQUEST);
        }


        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (isPBAPayment(caseData)) {
            log.info("Payment by PBA Number :  {}", caseData.getPbaNumber());
            boolean success = pbaPaymentService.makePayment(authToken, caseData.getPbaNumber(), Long.valueOf(caseData.getAmountToPay()));

            if (!pbaValidationService.isValidPBA(authToken, caseData.getPbaNumber())) {
                log.info("Payment by PBA number {} failed.", caseData.getPbaNumber());
                return ResponseEntity.ok(CCDCallbackResponse.builder()
                        .errors(Arrays.asList("PBA Account payment failed."))
                        .build());
            }
            log.info("Payment by PBA number succeeded.");
        }
        return ResponseEntity.ok(CCDCallbackResponse.builder().build());

    }

}
