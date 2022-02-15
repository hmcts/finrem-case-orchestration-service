package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_REF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class CaseDataController implements BaseController {

    private final UpdateSolicitorDetailsService solicitorService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final FeatureToggleService featureToggleService;

    @PostMapping(path = "/consented/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for consented journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setConsentedDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for consented journey.");
        setDefaultValues(callbackRequest, authToken);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/contested/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for contested journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setContestedDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested journey.");
        setDefaultValues(callbackRequest, authToken);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    private void setDefaultValues(CallbackRequest callbackRequest, String authToken) {
        validateCaseData(callbackRequest);
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        setData(authToken, caseData);
        setOrganisationPolicy(callbackRequest.getCaseDetails());
        setApplicantSolicitorOrganisationDetails(callbackRequest.getCaseDetails(), authToken);
    }

    @PostMapping(path = "/contested/set-frc-details",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set Financial Remedies Court details")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setFinancialRemediesCourtDetails(
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting Financial Remedies Court details.");
        caseDataService.setFinancialRemediesCourtDetails(callbackRequest.getCaseDetails());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/contested/set-paper-case-defaults",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for contested paper case journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setContestedPaperCaseDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested paper case journey.");
        validateCaseData(callbackRequest);
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        setData(authToken, caseData);
        setPaperCaseData(caseData);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/contested/set-paper-case-org-policy",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for contested paper case journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setContestedPaperCaseOrganisationPolicy(
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested paper case journey.");
        validateCaseData(callbackRequest);
        setOrganisationPolicyForNewPaperCase(callbackRequest.getCaseDetails());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/move-collection/{source}/to/{destination}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> moveValues(
        @RequestBody final CallbackRequest callbackRequest,
        @PathVariable("source") final String source,
        @PathVariable("destination") final String destination) {

        validateCaseData(callbackRequest);

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseDataService.moveCollection(caseData, source, destination);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/default-values",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Default application state")

    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> defaultValue(
        @RequestBody final CallbackRequest callbackRequest) {

        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void setData(final String authToken, final Map<String, Object> caseData) {
        if (idamService.isUserRoleAdmin(authToken)) {
            caseData.put(IS_ADMIN, YES_VALUE);
        } else {
            caseData.put(IS_ADMIN, NO_VALUE);
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
    }

    private void setPaperCaseData(Map<String, Object> caseData) {
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(FAST_TRACK_DECISION, NO_VALUE);
    }

    private void setOrganisationPolicy(CaseDetails caseDetails) {
        if  (caseDataService.isContestedApplication(caseDetails) || caseDataService.isConsentedApplication(caseDetails)) {
            Map<String, Object> appSolPolicy = buildOrganisationPolicy(APP_SOLICITOR_POLICY);
            caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, appSolPolicy);

            log.info("App Sol policy added to case: {}", appSolPolicy);
        }
    }

    private void setOrganisationPolicyForNewPaperCase(CaseDetails caseDetails) {
        if  (caseDataService.isContestedApplication(caseDetails) || caseDataService.isConsentedApplication(caseDetails)) {
            setOrganisationPolicy(caseDetails);

            Map<String, Object> appRespPolicy = buildOrganisationPolicy(RESP_SOLICITOR_POLICY);
            caseDetails.getData().put(ORGANISATION_POLICY_RESPONDENT, appRespPolicy);

            log.info("App Resp policy added to case: {}", appRespPolicy);
        }
    }

    private Map<String, Object> buildOrganisationPolicy(String caseAssignedRole) {
        Map<String, Object> appPolicy = new HashMap<>();
        appPolicy.put(ORGANISATION_POLICY_ROLE, caseAssignedRole);
        appPolicy.put(ORGANISATION_POLICY_REF, null);
        Map<String, Object> org = new HashMap<>();
        org.put(ORGANISATION_POLICY_ORGANISATION_ID, null);
        appPolicy.put(ORGANISATION_POLICY_ORGANISATION, org);

        return appPolicy;
    }

    private void setApplicantSolicitorOrganisationDetails(CaseDetails caseDetails, String authToken) {
        if (featureToggleService.isRespondentJourneyEnabled()
            && caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())) {
            solicitorService.setApplicantSolicitorOrganisationDetails(authToken, caseDetails);
        }
    }
}
