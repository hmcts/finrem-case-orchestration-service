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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CallbackDispatchService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class CcdCallbackController {

    private final CallbackDispatchService callbackDispatchService;


    @PostMapping(path = "/ccdAboutToStartEvent")
    @Operation(summary = "Handles AboutToStart callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdAboutToStart(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) {

        log.info("About to start Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        return performRequest(ABOUT_TO_START, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdAboutToSubmitEvent")
    @Operation(summary = "Handles AboutToSubmit callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdAboutToSubmit(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) {

        log.info("About to submit Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        return performRequest(ABOUT_TO_SUBMIT, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdMidEvent")
    @Operation(summary = "Handles MidEvent callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdMidEvent(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) {

        log.info("Mid Event Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        return performRequest(MID_EVENT, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdSubmittedEvent")
    @Operation(summary = "Handles Submitted callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdSubmittedEvent(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) {

        log.info("Submitted Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        return performRequest(SUBMITTED, callbackRequest, authorisationToken);
    }

    private void validateCaseData(CallbackRequest callbackRequest) {
        if (callbackRequest == null
            || callbackRequest.getCaseDetails() == null
            || callbackRequest.getCaseDetails().getData() == null) {
            throw new InvalidCaseDataException(BAD_REQUEST.value(), "Missing data from CallbackRequest.");
        }
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> performRequest(CallbackType callbackType,
                                                                                CallbackRequest callbackRequest,
                                                                                String userAuthorisation) {

        AboutToStartOrSubmitCallbackResponse callbackResponse =
            callbackDispatchService.dispatchToHandlers(callbackType, callbackRequest, userAuthorisation);

        log.info("Financial Remedy Case CCD callback `{}` handled for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        return ok(callbackResponse);
    }
}
