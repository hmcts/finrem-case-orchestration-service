package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


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
    @ApiOperation(value = "Handles AboutToStart callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdAboutToStart(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) {

        log.info("About to start Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        //TODO: add Authorisation service logic
        //authorisationService.authorise(authToken);

        return performRequest(ABOUT_TO_START, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdAboutToSubmitEvent")
    @ApiOperation(value = "Handles AboutToSubmit callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdAboutToSubmit(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) {

        log.info("About to submit Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        //TODO: add Authorisation service logic
        //authorisationService.authorise(authToken);

        return performRequest(ABOUT_TO_SUBMIT, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdMidEvent")
    @ApiOperation(value = "Handles MidEvent callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdMidEvent(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) {

        log.info("Mid Event Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        //TODO: add Authorisation service logic
        //authorisationService.authorise(authToken);

        return performRequest(MID_EVENT, callbackRequest, authorisationToken);
    }

    @PostMapping(path = "/ccdSubmittedEvent")
    @ApiOperation(value = "Handles Submitted callback requests from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> ccdSubmittedEvent(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest) {

        log.info("Submitted Financial Remedy case callback `{}` received for Case ID `{}`",
            callbackRequest.getEventId(),
            callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        //TODO: add Authorisation service logic
        //authorisationService.authorise(authToken);

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
