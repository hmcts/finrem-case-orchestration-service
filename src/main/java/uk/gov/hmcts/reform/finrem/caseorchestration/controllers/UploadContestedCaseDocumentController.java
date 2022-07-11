package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadCaseFilesAboutToSubmitHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class UploadContestedCaseDocumentController extends BaseController {

    private final UploadCaseFilesAboutToSubmitHandler uploadCaseFilesAboutToSubmitHandler;

    @PostMapping(path = "/upload-contested-case-documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles update Contested Case details and cleans up the data fields based on the options chosen for Contested Cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> uploadCaseDocuments(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false)
        @RequestBody CallbackRequest ccdRequest) {

        validateCaseData(ccdRequest);

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        Long caseId = caseDetails.getId();
        log.info("Received request to upload Contested case documents for Case ID: {}", caseId);

        AboutToStartOrSubmitCallbackResponse response =
            uploadCaseFilesAboutToSubmitHandler.handle(caseDetails.getData());
        log.info("Successfully filtered documents to relevant party for Case ID: {}", caseId);

        return ResponseEntity.ok(response);
    }
}
