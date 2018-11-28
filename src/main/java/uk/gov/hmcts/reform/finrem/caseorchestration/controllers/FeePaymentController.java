package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentByAccountService;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class FeePaymentController {
    private final FeeService feeService;
    private final PaymentByAccountService paymentByAccountService;

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> feeLookup(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for FEE lookup. Auth token: {}, Case request : {}", authToken, ccdRequest);

        if (!isValidCaseData(ccdRequest)) {
            return new ResponseEntity("Missing case data from CCD request.", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(new CCDCallbackResponse(ccdRequest.getCaseDetails().getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }

    private boolean isValidCaseData(CCDRequest ccdRequest) {
        return ccdRequest != null && ccdRequest.getCaseDetails() != null
                && ccdRequest.getCaseDetails().getCaseData() != null
                && ccdRequest.getCaseDetails().getCaseData().getDivorceCaseNumber() != null;
    }


    @PostMapping(
            path = "/pba-validate/{pbaNumber}",
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> pbaValidate(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @PathVariable String pbaNumber) {
        log.info("Received request for PBA validate. Auth token: {}, Case request : {}", authToken, pbaNumber);

        boolean validPBA = paymentByAccountService.isValidPBA(authToken, pbaNumber);
        log.info("validPBA:  {}", validPBA);

        return ResponseEntity.ok(validPBA);
    }

}
