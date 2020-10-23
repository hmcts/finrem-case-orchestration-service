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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_REF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isApplicantRepresentedByASolicitor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class CaseDataController implements BaseController {

    private final UpdateSolicitorDetailsService solicitorService;
    private final IdamService idamService;
    private final FeatureToggleService featureToggleService;

    @PostMapping(path = "/consented/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for consented journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setConsentedDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for consented journey.");
        validateCaseData(callbackRequest);
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        setData(authToken, caseData);
        setOrganisationPolicy(callbackRequest.getCaseDetails());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/contested/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set default values for contested journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setContestedDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested journey.");
        validateCaseData(callbackRequest);
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        setData(authToken, caseData);
        setOrganisationPolicy(callbackRequest.getCaseDetails());
        setApplicantSolicitorOrganisationDetails(callbackRequest.getCaseDetails(), authToken);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
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
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested paper case journey.");
        validateCaseData(callbackRequest);
        setOrganisationPolicy(callbackRequest.getCaseDetails());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/move-collection/{source}/to/{destination}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> moveValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest,
        @PathVariable("source") final String source,
        @PathVariable("destination") final String destination) {

        validateCaseData(callbackRequest);
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.get(source) != null && (caseData.get(source) instanceof Collection)) {
            if (caseData.get(destination) == null || (caseData.get(destination) instanceof Collection)) {
                final List destinationList = new ArrayList();
                if (caseData.get(destination) != null) {
                    destinationList.addAll((List) caseData.get(destination));
                }
                destinationList.addAll((List) caseData.get(source));
                caseData.put(destination, destinationList);
                caseData.put(source, null);
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void setData(final String authToken, final Map<String, Object> caseData) {
        if (idamService.isUserRoleAdmin(authToken)) {
            log.info("Admin users.");
            caseData.put(IS_ADMIN, YES_VALUE);
        } else {
            log.info("other users.");
            caseData.put(IS_ADMIN, NO_VALUE);
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
    }

    private void setPaperCaseData(Map<String, Object> caseData) {
        caseData.put(PAPER_APPLICATION, YES_VALUE);
        caseData.put(FAST_TRACK_DECISION, NO_VALUE);
    }

    private void setOrganisationPolicy(CaseDetails caseDetails) {
        log.info("Share a case is enabled: {}", featureToggleService.isShareACaseEnabled());
        if (featureToggleService.isShareACaseEnabled() && (isContestedApplication(caseDetails)
            || isConsentedApplication(caseDetails))) {

            Map<String, Object> appPolicy = new HashMap<>();
            appPolicy.put(ORGANISATION_POLICY_ROLE, APP_SOLICITOR_POLICY);
            appPolicy.put(ORGANISATION_POLICY_REF, null);
            Map<String, Object> org = new HashMap<>();
            org.put(ORGANISATION_POLICY_ORGANISATION_ID, null);
            org.put(ORGANISATION_POLICY_ORGANISATION_NAME, null);
            appPolicy.put(ORGANISATION_POLICY_ORGANISATION, org);

            caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, appPolicy);

            log.info("App policy added to case : {}", appPolicy);
        }
    }

    private void setApplicantSolicitorOrganisationDetails(CaseDetails caseDetails, String authToken) {
        if (featureToggleService.isShareACaseEnabled() && isContestedApplication(caseDetails)) {
            log.info("Share a case toggle is: {}", featureToggleService.isShareACaseEnabled());

            if (isApplicantRepresentedByASolicitor(caseDetails.getData())) {
                //this check could possibly be removed. I added the field into this file, it can probably be removed:
                // fixtures/contested/contested-upload-case-documents.json

                caseDetails.getData().put(CONTESTED_SOLICITOR_ADDRESS, solicitorService.updateApplicantSolicitorAddressFromPrd(authToken));
            }
        }
    }
}
