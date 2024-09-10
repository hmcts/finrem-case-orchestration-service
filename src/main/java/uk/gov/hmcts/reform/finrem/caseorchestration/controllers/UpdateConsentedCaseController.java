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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_RESIDE_OUTSIDE_UK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class UpdateConsentedCaseController extends BaseController {

    private final UpdateRepresentationWorkflowService nocWorkflowService;
    private final FeatureToggleService featureToggleService;

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

        updateApplicantOrSolicitorContactDetails(caseData);

        if (YES_VALUE.equals(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE))) {
            CaseDetails originalCaseDetails = ccdRequest.getCaseDetailsBefore();
            handleApplicantRepresentationChange(caseData);
            handleRespondentRepresentationChange(caseDetails);
            return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                authToken,
                originalCaseDetails));
        }

        persistOrgPolicies(caseData, ccdRequest.getCaseDetailsBefore());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private boolean isIncludesRepresentationChange(Map<String, Object> caseData) {
        return YES_VALUE.equals(caseData.get(INCLUDES_REPRESENTATION_CHANGE));
    }

    private void handleApplicantRepresentationChange(Map<String, Object> caseData) {
        String nocParty = (String) caseData.get(NOC_PARTY);
        if (APPLICANT.equalsIgnoreCase(nocParty)) {
            removeApplicantSolicitorDetails(caseData);
        }
    }

    private void handleRespondentRepresentationChange(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String nocParty = (String) caseData.get(NOC_PARTY);
        if (RESPONDENT.equalsIgnoreCase(nocParty)) {
            removeRespondentDetails(caseData, caseDetails.getCaseTypeId());
        }
    }

    private void removeApplicantSolicitorDetails(Map<String, Object> caseData) {
        String applicantRepresented = nullToEmpty(caseData.get(APPLICANT_REPRESENTED));
        if (applicantRepresented.equals(NO_VALUE)) {
            caseData.remove("applicantSolicitorName");
            caseData.remove("applicantSolicitorFirm");
            caseData.remove("applicantSolicitorAddress");
            caseData.remove("applicantSolicitorPhone");
            caseData.remove("applicantSolicitorEmail");
            caseData.remove("applicantSolicitorDXnumber");
            caseData.remove("applicantSolicitorConsentForEmails");
            caseData.remove(APPLICANT_ORGANISATION_POLICY);
        }
    }

    private void removeRespondentDetails(Map<String, Object> caseData, String caseTypeId) {
        boolean isContested = caseTypeId.equalsIgnoreCase(CaseType.CONTESTED.getCcdType());
        String respondentRepresented = isContested
            ? (String) caseData.get(CONTESTED_RESPONDENT_REPRESENTED)
            : (String) caseData.get(CONSENTED_RESPONDENT_REPRESENTED);
        if (respondentRepresented.equals(YES_VALUE)) {
            caseData.remove(RESPONDENT_ADDRESS);
            caseData.remove(RESPONDENT_PHONE);
            caseData.remove(RESPONDENT_EMAIL);
            caseData.put(RESPONDENT_RESIDE_OUTSIDE_UK, NO_VALUE);
        } else {
            caseData.remove(RESP_SOLICITOR_NAME);
            caseData.remove(RESP_SOLICITOR_FIRM);
            caseData.remove(RESP_SOLICITOR_ADDRESS);
            caseData.remove(RESP_SOLICITOR_PHONE);
            caseData.remove(RESP_SOLICITOR_EMAIL);
            caseData.remove(RESP_SOLICITOR_DX_NUMBER);
            caseData.remove(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT);
            caseData.remove(RESPONDENT_ORGANISATION_POLICY);
        }
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
