package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeApplicantSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateConsentedCaseController extends BaseController {

    @Autowired
    private UpdateRepresentationWorkflowService nocWorkflowService;

    @Autowired
    private FeatureToggleService featureToggleService;

    @PostMapping(path = "/update-contact-details", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);
        Map<String, Object> caseData = caseDetails.getData();


        updateRespondentSolicitorAddress(caseData);
        log.info("DEBUGGING NOC - updateRespondentSolicitorAddress entered and applicant name is still present",
            caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME) != null);
        updateApplicantOrSolicitorContactDetails(caseData);
        log.info("DEBUGGING NOC - updateApplicantOrSolicitorContactDetails entered and applicant name is still present",
            caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME) != null);

        if (featureToggleService.isCaseworkerNoCEnabled()
            && ofNullable(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE)).isPresent()
            && caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE)) {
            CaseDetails originalCaseDetails = ccdRequest.getCaseDetailsBefore();
            return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                authToken,
                originalCaseDetails));
        }
        log.info("DEBUGGING NOC - after conditional entered and applicant name is still present",
            caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME) != null,
            ofNullable(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE)).isPresent(),
            caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE));
        persistOrgPolicies(caseData, ccdRequest.getCaseDetailsBefore());

        log.info("DEBUGGING NOC - persistOrgPolicies entered and applicant name is still present",
            caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME) != null);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void updateRespondentSolicitorAddress(Map<String, Object> caseData) {
        if (NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED)))) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeRespondentAddress(caseData);
        }
    }

    private void updateApplicantOrSolicitorContactDetails(Map<String, Object> caseData) {
        Optional<Object> applicantRepresented = ofNullable(caseData.get(APPLICANT_REPRESENTED));
        if (equalsTo(Objects.toString(applicantRepresented.orElse("No")), "No")) {
            removeApplicantSolicitorAddress(caseData, false);
        } else {
            removeApplicantAddress(caseData);
        }
    }

    private void removeApplicantAddress(Map<String, Object> caseData) {
        caseData.put(APPLICANT_ADDRESS, null);
        caseData.put(APPLICANT_PHONE, null);
        caseData.put(APPLICANT_EMAIL, null);
        caseData.put(APPLICANT_RESIDE_OUTSIDE_UK, YesOrNo.NO);
    }

    private void removeRespondentAddress(Map<String, Object> caseData) {
        caseData.put(RESPONDENT_ADDRESS, null);
        caseData.put(RESPONDENT_PHONE, null);
        caseData.put(RESPONDENT_EMAIL, null);
        caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, YesOrNo.NO);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private void persistOrgPolicies(Map<String, Object> caseData, CaseDetails originalDetails) {
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        caseData.put(RESPONDENT_ORGANISATION_POLICY, originalDetails.getData().get(RESPONDENT_ORGANISATION_POLICY));
    }
}
