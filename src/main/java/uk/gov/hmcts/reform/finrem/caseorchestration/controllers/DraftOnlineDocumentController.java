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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_REF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ROLE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class DraftOnlineDocumentController {

    private final OnlineFormDocumentService service;
    private final IdamService idamService;
    private final CaseFlagsService caseFlagsService;

    @PostMapping(path = "/documents/draft-contested-mini-form-a", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles draft Contested Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateContestedMiniFormA(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        log.info("Received request to generate draft Contested Mini Form A for Case ID : {}",
            callback.getCaseDetails().getId());

        CaseDetails caseDetails = callback.getCaseDetails();
        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        if (!idamService.isUserRoleAdmin(authorisationToken)) {
            log.info("other users.");
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
        CaseDocument document = service.generateDraftContestedMiniFormA(authorisationToken, callback.getCaseDetails());
        caseData.put(MINI_FORM_A, document);

        setDefaultOrgIfNotSetAlready(caseData, ORGANISATION_POLICY_APPLICANT, CaseRole.APP_SOLICITOR.getCcdCode());
        setDefaultOrgIfNotSetAlready(caseData, ORGANISATION_POLICY_RESPONDENT, CaseRole.RESP_SOLICITOR.getCcdCode());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void setDefaultOrgIfNotSetAlready(Map<String, Object> caseData, String policy, String caseAssignedRole) {
        Map<String, Object> partyPolicy = (Map<String, Object>) caseData.get(policy);
        if (partyPolicy == null) {
            Map<String, Object> partyPolicyObj = buildOrganisationPolicy(caseAssignedRole);
            caseData.put(policy, partyPolicyObj);
        }
    }

    private Map<String, Object> buildOrganisationPolicy(String caseAssignedRole) {
        Map<String, Object> policy = new HashMap<>();
        policy.put(ORGANISATION_POLICY_ROLE, caseAssignedRole);
        policy.put(ORGANISATION_POLICY_REF, null);
        Map<String, Object> org = new HashMap<>();
        org.put(ORGANISATION_POLICY_ORGANISATION_ID, null);
        policy.put(ORGANISATION_POLICY_ORGANISATION, org);
        return policy;
    }
}
