package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class ConsentOrderController implements BaseController {

    @Autowired
    private ConsentOrderService consentOrderService;

    @PostMapping(path = "/update-latest-consent-order", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "CCD Callback to update the latest consent order d    etails")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Callback was processed successFully or"
            + " in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateLatestConsentOrderDetails(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for update latest consent order. Auth token: {}, Case request : {}",
                authToken, callbackRequest);

        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.put("latestConsentOrder", caseDocument);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
