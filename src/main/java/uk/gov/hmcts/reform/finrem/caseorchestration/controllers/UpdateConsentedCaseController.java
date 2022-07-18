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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeApplicantSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateConsentedCaseController extends BaseController {

    @Autowired private UpdateRepresentationWorkflowService nocWorkflowService;

    @Autowired private FeatureToggleService featureToggleService;

    @PostMapping(path = "/update-contact-details", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);
        Map<String, Object> caseData = caseDetails.getData();

        updateRespondentSolicitorAddress(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);

        if (featureToggleService.isCaseworkerNoCEnabled()) {

            if (Optional.ofNullable(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE)).isPresent()
                && caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE)) {
                CaseDetails originalCaseDetails = ccdRequest.getCaseDetailsBefore();
                return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                    authToken,
                    originalCaseDetails));
            }

        }

        persistOrgPolicies(caseData, ccdRequest.getCaseDetailsBefore());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/update-case-solicitor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateCaseSolicitor(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case solicitor contact details with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(removeSolicitorAddress(caseDetails, false)).build());
    }


    private void updateRespondentSolicitorAddress(Map<String, Object> caseData) {
        if (NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED)))) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeRespondentAddress(caseData);
        }
    }

    private void updateApplicantOrSolicitorContactDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(APPLICANT_REPRESENTED), "No")) {
            removeApplicantSolicitorAddress(caseData, false);
        } else {
            removeApplicantAddress(caseData);
        }
    }


    private void removeApplicantAddress(Map<String, Object> caseData) {
        caseData.put(APPLICANT_ADDRESS, null);
        caseData.put(APPLICANT_PHONE, null);
        caseData.put(APPLICANT_EMAIL, null);
    }

    private void removeRespondentAddress(Map<String, Object> caseData) {
        caseData.put(RESPONDENT_ADDRESS, null);
        caseData.put(RESPONDENT_PHONE, null);
        caseData.put(RESPONDENT_EMAIL, null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private void persistOrgPolicies(Map<String, Object> caseData, CaseDetails originalDetails) {
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        caseData.put(RESPONDENT_ORGANISATION_POLICY, originalDetails.getData().get(RESPONDENT_ORGANISATION_POLICY));
    }
}
