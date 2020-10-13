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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationController implements BaseController {

    private final GeneralApplicationService generalApplicationService;

    @PostMapping(path = "/submit-general-application", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Submit general application")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> submitGeneralApplication(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to submit general application for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        log.info("2 GENERAL_APPLICATION_DIRECTIONS_DOCUMENT is {}", caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));

        CaseDetails caseDetailsBefore = callback.getCaseDetailsBefore();
        generalApplicationService.updateCaseDataSubmit(caseDetails.getData(), caseDetailsBefore);

        log.info("3 GENERAL_APPLICATION_DIRECTIONS_DOCUMENT is {}", caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getData())
            .build());
    }

    @PostMapping(path = "/start-general-application", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Start general application")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> startGeneralApplication(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to start general application for Case ID: {}", caseDetails.getId());
        log.info("A ----------");
        validateCaseData(callback);
        log.info("B ----------");

        log.info("C ----------");
        log.info("0 GENERAL_APPLICATION_DIRECTIONS_DOCUMENT is {}", caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));

        generalApplicationService.updateCaseDataStart(caseDetails.getData(), authorisationToken);

        log.info("1a GENERAL_APPLICATION_DIRECTIONS_DOCUMENT is {}", caseDetails.getData().get(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getData())
            .build());
    }
}
