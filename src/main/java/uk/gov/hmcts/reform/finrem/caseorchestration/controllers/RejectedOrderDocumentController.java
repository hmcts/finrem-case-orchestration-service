package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class RejectedOrderDocumentController {

    @Autowired
    private RefusalOrderDocumentService service;

    @PostMapping(path = "/documents/consent-order-not-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consent Order Not Approved Order Generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                message = "Callback was processed successfully or in case of an error message is attached to the case",
                response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateConsentOrderNotApproved(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
            @RequestBody @ApiParam("CaseData") CallbackRequest request) {

        CaseDetails caseDetails = request.getCaseDetails();
        log.info("Received request to generate 'Consent Order Not Approved' for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = service.generateConsentOrderNotApproved(authorisationToken, caseDetails);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build()
        );
    }

    @PostMapping(path = "/documents/preview-consent-order-not-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles preview Consent Order Not Approved Order Generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                message = "Callback was processed successfully or in case of an error message is attached to the case",
                response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> previewConsentOrderNotApproved(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
            @RequestBody @ApiParam("CaseData") CallbackRequest request) {

        CaseDetails caseDetails = request.getCaseDetails();
        log.info("Received request to preview generated 'Consent Order Not Approved' for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = service.previewConsentOrderNotApproved(authorisationToken, caseDetails);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build()
        );
    }
}
