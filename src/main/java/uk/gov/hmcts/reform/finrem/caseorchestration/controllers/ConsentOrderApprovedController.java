package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import javax.validation.constraints.NotNull;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController extends BaseController {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderPrintService consentOrderPrintService;

    @PostMapping(path = "/consent-in-contested/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' callback handler for consent in contested. Stamps Consent Order Approved documents"
        + "and adds them to a collection")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedConsentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        validateCaseData(callback);
        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        consentOrderApprovedDocumentService.stampAndPopulateContestedConsentApprovedOrderCollection(caseData, authToken);
        consentOrderApprovedDocumentService.generateAndPopulateConsentOrderLetter(caseDetails, authToken);

        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of())
                .build());
    }

    @PostMapping(path = "/consent-in-contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "'Consent Order Approved' and 'Consent Order Not Approved' callback handler for consent in contested. "
        + "Checks state and if not/approved generates docs else puts latest general order into uploadOrder fields. "
        + "Then sends the data to bulk print")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedSendOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();

        consentOrderPrintService.sendConsentOrderToBulkPrint(caseDetails, authToken);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build());
    }
}
