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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Map;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class RemoveApplicantDetailsController extends BaseController {

    private final UpdateContactDetailsService updateContactDetailsService;
    private final UpdateRepresentationWorkflowService nocWorkflowService;
    private final OnlineFormDocumentService service;

    @PostMapping(path = "/remove-details", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Removes applicant details or applicants solicitor details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> removeDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for removing Applicant/Applicants Solicitor details for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();

        boolean includesRepresentationChange = updateContactDetailsService.isIncludesRepresentationChange(caseData);
        if (includesRepresentationChange) {
            updateContactDetailsService.handleApplicantRepresentationChange(caseDetails);
            updateContactDetailsService.handleRespondentRepresentationChange(caseDetails);
        }

        String applicantConfidentialAddress = Objects.toString(caseData.get(APPLICANT_CONFIDENTIAL_ADDRESS), null);
        String respondentConfidentialAddress = Objects.toString(caseData.get(RESPONDENT_CONFIDENTIAL_ADDRESS), null);
        if (applicantConfidentialAddress != null && applicantConfidentialAddress.equalsIgnoreCase(YES_VALUE)
            || respondentConfidentialAddress != null && respondentConfidentialAddress.equalsIgnoreCase(YES_VALUE)) {
            CaseDocument document = service.generateContestedMiniFormA(authorisationToken, callback.getCaseDetails());
            caseData.put(MINI_FORM_A, document);
        }

        if (includesRepresentationChange) {
            return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails, authorisationToken,
                callback.getCaseDetailsBefore()));
        } else {
            updateContactDetailsService.persistOrgPolicies(caseData, callback.getCaseDetailsBefore());
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
        }
    }
}
