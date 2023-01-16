package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INCLUDES_REPRESENTATION_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
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
public class RemoveApplicantDetailsController extends BaseController {

    @Autowired
    private final UpdateRepresentationWorkflowService nocWorkflowService;

    @Autowired
    private final FeatureToggleService featureToggleService;

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

        removeApplicantDetails(caseData);
        removeRespondentDetails(caseData, caseDetails.getCaseTypeId());

        if (featureToggleService.isCaseworkerNoCEnabled()) {

            if (Optional.ofNullable(caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE)).isPresent()
                && caseDetails.getData().get(INCLUDES_REPRESENTATION_CHANGE).equals(YES_VALUE)) {
                CaseDetails originalCaseDetails = callback.getCaseDetailsBefore();
                return ResponseEntity.ok(nocWorkflowService.handleNoticeOfChangeWorkflow(caseDetails,
                    authorisationToken,
                    originalCaseDetails));
            }

        }

        persistOrgPolicies(caseData, callback.getCaseDetailsBefore());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void removeApplicantDetails(Map<String, Object> caseData) {
        String applicantRepresented = nullToEmpty(caseData.get(APPLICANT_REPRESENTED));
        if (applicantRepresented.equals(YES_VALUE)) {
            //remove applicants data as solicitors data has been added
            caseData.remove("applicantAddress");
            caseData.remove("applicantPhone");
            caseData.remove("applicantEmail");
        } else {
            caseData.remove("applicantSolicitorName");
            caseData.remove("applicantSolicitorFirm");
            caseData.remove("applicantSolicitorAddress");
            caseData.remove("applicantSolicitorPhone");
            caseData.remove("applicantSolicitorEmail");
            caseData.remove("applicantSolicitorDXnumber");
            caseData.remove("applicantSolicitorConsentForEmails");
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
        } else {
            caseData.remove(RESP_SOLICITOR_NAME);
            caseData.remove(RESP_SOLICITOR_FIRM);
            caseData.remove(RESP_SOLICITOR_ADDRESS);
            caseData.remove(RESP_SOLICITOR_PHONE);
            caseData.remove(RESP_SOLICITOR_EMAIL);
            caseData.remove(RESP_SOLICITOR_DX_NUMBER);
            caseData.remove(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT);
        }
    }

    private void persistOrgPolicies(Map<String, Object> caseData, CaseDetails originalDetails) {
        caseData.put(APPLICANT_ORGANISATION_POLICY, originalDetails.getData().get(APPLICANT_ORGANISATION_POLICY));
        caseData.put(RESPONDENT_ORGANISATION_POLICY, originalDetails.getData().get(RESPONDENT_ORGANISATION_POLICY));
    }
}
