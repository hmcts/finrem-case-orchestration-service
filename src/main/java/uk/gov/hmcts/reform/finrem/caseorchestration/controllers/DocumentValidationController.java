package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class DocumentValidationController extends BaseController {

    private final DocumentValidationService service;
    private final ConsentedApplicationHelper helper;

    @PostMapping(path = "/field/{field}/file-upload-check", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Checks the file type and returns error.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkUploadedFileType(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest,
        @PathVariable("field") String field) {

        Optional<Long> caseId = Optional.ofNullable(callbackRequest.getCaseDetails().getId());
        log.info("Received request for checkUploadedFileType for Case ID: {}", caseId);

        validateCaseData(callbackRequest);
        if (Boolean.TRUE.equals(isConsentedApplication(callbackRequest.getCaseDetails()))) {
            helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());
        }
        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(callbackRequest.getCaseDetails().getData())
                .build());
    }

    private Boolean isConsentedApplication(CaseDetails caseDetails) {
        return CaseType.CONSENTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }
}
