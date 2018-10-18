package uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.fee.Fee;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.FeeService;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.IDAMService;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.PBAService;

import java.util.ArrayList;
import javax.ws.rs.core.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-progression")
@Slf4j
public class CcdCallbackController {
    private final FeeService feeService;
    private final PBAService pbaService;

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> feeLookup(@RequestHeader(value = "Authorization", required = false) String authToken,
                                                         @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for FEE lookup. Auth token: {}, Case request : {}", authToken, ccdRequest);

        Fee fee = feeService.getApplicationFee();

        ccdRequest.getCaseDetails().getCaseData().setFeeAmountToPay(fee.getFeeAmount().toString());

        return ResponseEntity.ok(new CCDCallbackResponse(ccdRequest.getCaseDetails().getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }


    @PostMapping(path = "/pba-validate/{pbaNumber}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<Boolean> pbaValidate(@RequestHeader(value = "Authorization", required = false) String authToken,
                                               @PathVariable String pbaNumber) {
        log.info("Received request for PBA validate. Auth token: {}, Case request : {}", authToken, pbaNumber);

        boolean validPBA = pbaService.isValidPBA(authToken, pbaNumber);
        log.info("validPBA:  {}", validPBA);


        return ResponseEntity.ok(validPBA);
    }

}
