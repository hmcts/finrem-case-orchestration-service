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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationDirectionsController extends BaseController {

    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    @PostMapping(path = "/submit-general-application-directions", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit general application directions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> submitGeneralApplication(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to submit general application directions for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        List<String> errors = new ArrayList<>();
        try {
            generalApplicationDirectionsService.submitGeneralApplicationDirections(caseDetails, authorisationToken);
        } catch (InvalidCaseDataException invalidCaseDataException) {
            errors.add(invalidCaseDataException.getMessage());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getCaseData())
            .errors(errors)
            .build());
    }

    @PostMapping(path = "/start-general-application-directions", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Start general application directions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> startGeneralApplication(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to start general application directions for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        generalApplicationDirectionsService.startGeneralApplicationDirections(caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getCaseData())
            .build());
    }

    @PostMapping(path = "/submit-for-interim-hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "submit for interim hearing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> submitInterimHearing(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to submit for interim hearing for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        List<String> errors = new ArrayList<>();
        try {
            generalApplicationDirectionsService.submitInterimHearing(caseDetails, authorisationToken);
        } catch (InvalidCaseDataException invalidCaseDataException) {
            errors.add(invalidCaseDataException.getMessage());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetails.getCaseData())
            .errors(errors)
            .build());
    }
}
