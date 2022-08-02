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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterController extends BaseController {

    private final IdamService idamService;
    private final GeneralLetterService generalLetterService;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    @PostMapping(path = "/general-letter-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Clears previous entered field values. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> initialiseGeneralLetterProperties(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to clear general letter fields for Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();

        caseData.getGeneralLetterWrapper().setGeneralLetterAddressTo(null);
        caseData.getGeneralLetterWrapper().setGeneralLetterRecipient(null);
        caseData.getGeneralLetterWrapper().setGeneralLetterRecipientAddress(null);
        caseData.getGeneralLetterWrapper().setGeneralLetterCreatedBy(idamService.getIdamFullName(authorisationToken));
        caseData.getGeneralLetterWrapper().setGeneralLetterBody(null);
        caseData.getGeneralLetterWrapper().setGeneralLetterPreview(null);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/documents/preview-general-letter", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Preview general letter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> previewGeneralLetter(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to preview general letter for Case ID: {}", caseDetails.getId());

        if (generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails).isEmpty()) {
            generalLetterService.previewGeneralLetter(authorisationToken, caseDetails);
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build());
        } else {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
                .build());
        }
    }

    @PostMapping(path = "/documents/general-letter", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates general letter for case worker. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> createGeneralLetter(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for generating general letter with Case ID: {}", caseDetails.getId());

        if (generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails).isEmpty()) {
            generalLetterService.createGeneralLetter(authorisationToken, caseDetails);
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build());
        } else {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
                .build());
        }
    }
}
