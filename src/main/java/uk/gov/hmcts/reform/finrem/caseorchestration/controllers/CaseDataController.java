package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_REF;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class CaseDataController extends BaseController {

    private static final String CHANGE_ORGANISATION_REQUEST = "changeOrganisationRequestField";
    private final UpdateSolicitorDetailsService solicitorService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final BulkPrintDocumentService service;
    private final ConsentOrderService consentOrderService;

    @PostMapping(path = "/consented/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Set default values for consented journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setConsentedDefaultValues(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for consented journey.");
        setDefaultValues(callbackRequest, authToken);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/contested/set-defaults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Set default values for contested journey")
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
    @Operation(description = "Set Financial Remedies Court details")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setFinancialRemediesCourtDetails(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken,
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting Financial Remedies Court details.");
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        String region  =  ObjectUtils.nullSafeConciseToString(data.get(REGION));
        String cfcCourList  =  ObjectUtils.nullSafeConciseToString(data.get(CFC_COURTLIST));
        List<String> errors = new ArrayList<>();
        if (!idamService.isUserRoleAdmin(authToken) && (HIGHCOURT.equals(region) || (LONDON.equals(region)
            && cfcCourList.equals("FR_s_CFCList_17")))) {
            errors.add("You cannot select High Court or Royal Court of Justice. Please select another court.");
        }
        caseDataService.setFinancialRemediesCourtDetails(caseDetails);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(data).errors(errors).build());
    }

    @PostMapping(path = "/contested/set-paper-case-org-policy",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Set default values for contested paper case journey")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setContestedPaperCaseOrganisationPolicy(
        @RequestBody final CallbackRequest callbackRequest) {
        log.info("Setting default values for contested paper case journey.");
        validateCaseData(callbackRequest);
        setOrganisationPolicyForNewPaperCase(callbackRequest.getCaseDetails());
        callbackRequest.getCaseDetails().getData().putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
        callbackRequest.getCaseDetails().getData().putIfAbsent(URGENT_CASE_QUESTION, NO_VALUE);
        callbackRequest.getCaseDetails().getData().putIfAbsent(TYPE_OF_APPLICATION, TYPE_OF_APPLICATION_DEFAULT_TO);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build());
    }

    @PostMapping(path = "/org-policies", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Add empty org policies for both parties")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setOrgPolicies(
        @RequestBody CallbackRequest callbackRequest,
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) final String authToken
    ) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();

        addDefaultChangeOrganisationRequest(caseData);

        addOrganisationPoliciesIfPartiesNotRepresented(caseData);
        List<String> errors = new ArrayList<>();
        Map<String, Object> caseDataBefore = new HashMap<>();
        List<CaseDocument> caseDocuments = consentOrderService.checkIfD81DocumentContainsEncryption(caseData, caseDataBefore);
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            caseDocuments.forEach(document -> service.validateEncryptionOnUploadedDocument(document, "na", errors, authToken));
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).errors(errors).build());
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

    private void addOrganisationPoliciesIfPartiesNotRepresented(Map<String, Object> caseData) {

        if (!caseDataService.isApplicantRepresentedByASolicitor(caseData)) {
            OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
                .build();
            caseData.put(APPLICANT_ORGANISATION_POLICY, applicantOrganisationPolicy);
        }
        if (!caseDataService.isRespondentRepresentedByASolicitor(caseData)) {
            OrganisationPolicy respondentOrganisationPolicy = OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
                .build();
            caseData.put(RESPONDENT_ORGANISATION_POLICY, respondentOrganisationPolicy);
        }
    }

    private void setData(final String authToken, final Map<String, Object> caseData) {
        if (idamService.isUserRoleAdmin(authToken)) {
            caseData.put(IS_ADMIN, YES_VALUE);
        } else {
            caseData.put(IS_ADMIN, NO_VALUE);
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
    }

    private void setOrganisationPolicy(CaseDetails caseDetails) {
        if (caseDataService.isContestedApplication(caseDetails) || caseDataService.isConsentedApplication(caseDetails)) {
            Map<String, Object> appSolPolicy = buildOrganisationPolicy(APP_SOLICITOR_POLICY);
            caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, appSolPolicy);

            log.info("App Sol policy added to case: {}", appSolPolicy);
        }
    }

    private void setOrganisationPolicyForNewPaperCase(CaseDetails caseDetails) {
        if (caseDataService.isContestedApplication(caseDetails) || caseDataService.isConsentedApplication(caseDetails)) {
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
        if (caseDataService.isApplicantRepresentedByASolicitor(caseDetails.getData())) {
            solicitorService.setApplicantSolicitorOrganisationDetails(authToken, caseDetails);
        }
    }
}
