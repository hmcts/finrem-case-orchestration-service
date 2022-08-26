package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.OldAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OldCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class DocumentValidationController extends BaseController {

    @Autowired
    private DocumentValidationService service;
    @Autowired
    private ConsentedApplicationHelper helper;

    @PostMapping(path = "/field/{field}/file-upload-check", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Checks the file type and returns error.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully.",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OldAboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})

    public ResponseEntity<OldAboutToStartOrSubmitCallbackResponse> checkUploadedFileType(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") OldCallbackRequest callbackRequest,
        @PathVariable("field") String field) {

        Optional<Long> caseId = Optional.ofNullable(callbackRequest.getCaseDetails().getId());
        log.info("Received request for checkUploadedFileType for Case ID: {}", caseId);

        validateCaseData(callbackRequest);
        if (Boolean.TRUE.equals(isConsentedApplication(callbackRequest.getCaseDetails()))) {
            helper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());
        }
        return ResponseEntity.ok(response(callbackRequest, field, authorisationToken));
    }

    private OldAboutToStartOrSubmitCallbackResponse response(OldCallbackRequest callbackRequest, String field, String authorisationToken) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        OldAboutToStartOrSubmitCallbackResponse.OldAboutToStartOrSubmitCallbackResponseBuilder builder =
            OldAboutToStartOrSubmitCallbackResponse.builder().data(caseData);
        if (nonNull(caseData.get(field))) {
            DocumentValidationResponse response = service.validateDocument(callbackRequest, field, authorisationToken);
            return builder.errors(response.getErrors()).build();
        }
        return builder.build();
    }

    private Boolean isConsentedApplication(CaseDetails caseDetails) {
        return CASE_TYPE_ID_CONSENTED.equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }
}
