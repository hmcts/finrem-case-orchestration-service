package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateRepresentationService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATIVE_UPDATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateRepresentationController implements BaseController {

    @Autowired
    private UpdateRepresentationService updateRepresentationService;

    @Autowired
    private AssignCaseAccessService assignCaseAccessService;

    @Autowired
    private FeatureToggleService featureToggleService;

    @PostMapping(path = "/apply-noc-decision", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Applies Notice of Change Decision when initiated by solicitor and saves new sol's details to case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateRepresentation(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to apply Notice of Change Decision and update representation for case {}",
            caseDetails.getId());

        validateCaseData(ccdRequest);
        assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        Map<String, Object> caseData = updateRepresentationService.updateRepresentationAsSolicitor(caseDetails, authToken);
        caseDetails.getData().putAll(caseData);
        return ResponseEntity.ok(assignCaseAccessService.applyDecision(authToken, caseDetails));
    }

    @PostMapping(path = "/set-update-defaults")
    @ApiOperation(value = "Sets default values for update contact details event")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setNocDefaults(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authToken,
        @RequestBody CallbackRequest ccdRequest) {
        log.info("Received request to set default values for Update Contact Details Event for case {}",
            ccdRequest.getCaseDetails().getId());

        Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        validateCaseData(ccdRequest);

        if (featureToggleService.isCaseworkerNoCEnabled()) {
            caseData.put(NOC_PARTY, null);
            caseData.put(INCLUDES_REPRESENTATIVE_UPDATE, null);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/clear-noc-requests")
    @ApiOperation(value = "Clears current noc requests")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> clearNocRequests(
        @RequestBody CallbackRequest ccdRequest) {
        log.info("Received request to set default values for Update Contact Details Event for case {}",
            ccdRequest.getCaseDetails().getId());

        Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        validateCaseData(ccdRequest);

        if (featureToggleService.isCaseworkerNoCEnabled() || featureToggleService.isSolicitorNoticeOfChangeEnabled()) {
            addDefaultChangeOrganisationRequest(caseData);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void addDefaultChangeOrganisationRequest(Map<String, Object> caseData) {
        ChangeOrganisationRequest defaultChangeRequest = ChangeOrganisationRequest
            .builder()
            .requestTimestamp(null)
            .approvalRejectionTimestamp(null)
            .caseRoleId(null)
            .approvalStatus(null)
            .organisationToAdd(null)
            .organisationToRemove(null)
            .reason(null)
            .build();
        caseData.put(CHANGE_ORGANISATION_REQUEST, defaultChangeRequest);
    }
}
