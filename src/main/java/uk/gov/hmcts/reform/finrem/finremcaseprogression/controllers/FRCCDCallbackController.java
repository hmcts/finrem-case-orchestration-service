package uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.fee.Fee;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.FeeService;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/caseprogression")
@Slf4j
public class FRCCDCallbackController {
    private final FeeService feeService;

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> addCase(@RequestHeader(value = "Authorization", required = false) String authorizationToken,
                                                       @RequestBody CCDRequest ccdRequest) {
        log.info("Received request with a case-added event. Auth token: {}, Case request : {}" , authorizationToken, ccdRequest);

        Fee fee = feeService.getApplicationFee();

        ccdRequest.getCaseDetails().getCaseData().setFeeAmountToPay(fee.getFeeAmount().toString());

        return ResponseEntity.ok(new CCDCallbackResponse(ccdRequest.getCaseDetails().getCaseData(), new ArrayList<>(), new ArrayList<>()));
    }

}
