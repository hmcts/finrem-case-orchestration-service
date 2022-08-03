package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.ConsentOrderApprovedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
public class ConsentOrderApprovedController extends BaseController {

    private final FinremCallbackRequestDeserializer callbackRequestDeserializer;
    private final ConsentOrderApprovedHandler consentOrderApprovedHandler;

    @PostMapping(path = "/documents/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' callback handler. Generates relevant Consent Order Approved documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(source);
        validateCaseData(callbackRequest);

        return ResponseEntity.ok(consentOrderApprovedHandler.handleConsentOrderApproved(callbackRequest, authToken));
    }

    @PostMapping(path = "/consent-in-contested/consent-order-approved", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' callback handler for consent in contested. Stamps Consent Order Approved documents\"\n"
        + "        + \"and adds them to a collection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedConsentOrderApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callbackRequest = callbackRequestDeserializer.deserialize(source);
        validateCaseData(callbackRequest);

        return ResponseEntity.ok(consentOrderApprovedHandler.handleConsentInContestConsentOrderApproved(callbackRequest, authToken));
    }

    @PostMapping(path = "/consent-in-contested/send-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "'Consent Order Approved' and 'Consent Order Not Approved' callback handler for consent in contested. \"\n"
        + "        + \"Checks state and if not/approved generates docs else puts latest general order into uploadOrder fields. \"\n"
        + "        + \"Then sends the data to bulk print")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> consentInContestedSendOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);

        return ResponseEntity.ok(consentOrderApprovedHandler.handleConsentInContestSendOrder(callback, authToken));
    }
}
