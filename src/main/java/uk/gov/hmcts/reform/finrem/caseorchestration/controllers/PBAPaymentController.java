package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SUBMIT_CASE_DATE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAPaymentController extends BaseController {

    private static final String MIAM_INVALID_LEGACY_EXEMPTIONS_ERROR_MESSAGE =
        "The following MIAM exemptions that are no longer valid. Please re-submit the exemptions using Amend Application Details.";

    private final AssignCaseAccessService assignCaseAccessService;
    private final CcdDataStoreService ccdDataStoreService;
    private final PrdOrganisationService prdOrganisationService;
    private final MiamLegacyExemptionsService miamLegacyExemptionsService;

    @PostMapping(path = "/assign-applicant-solicitor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles assign applicant solicitor call")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> applicantOrganisationCheck(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for assign applicant solicitor for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);
        final Map<String, Object> mapOfCaseData = caseDetails.getData();
        if (miamLegacyExemptionsService.isLegacyExemptionsInvalid(mapOfCaseData)) {
            return miamInvalidLegacyExemptionsFailure(mapOfCaseData);
        }

        return assignApplicantSolicitor(caseDetails, authToken);
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> assignApplicantSolicitor(CaseDetails caseDetails,
                                                                                          String authToken) {
        if (assignCaseAccessService.isCreatorRoleActiveOnCase(caseDetails)) {
            try {
                String applicantOrgId = getApplicantOrgId(caseDetails);
                String respondentOrgId = getRespondentOrgId(caseDetails);

                if (applicantOrgId != null) {
                    OrganisationsResponse prdOrganisation = prdOrganisationService.retrieveOrganisationsData(authToken);

                    // Check if the applicant organisation and respondent organisation are the same if the respondent is represented
                    if(isRespondentRepresented(caseDetails) && (respondentOrgId != null && applicantOrgId.equals(respondentOrgId))) {
                        String errorMessage = "Applicant organisation cannot be the same as respondent organisation";
                        return assignCaseAccessFailure(caseDetails, singletonList(errorMessage));
                    }

                    if (prdOrganisation.getOrganisationIdentifier().equals(applicantOrgId)) {
                        log.info("Assigning case access for Case ID: {}", caseDetails.getId());
                        try {
                            assignCaseAccessService.assignCaseAccess(caseDetails, authToken);
                            ccdDataStoreService.removeCreatorRole(caseDetails, authToken);
                        } catch (Exception e) {
                            log.info("Assigning case access threw exception for Case ID: {}, {}",
                                caseDetails.getId(), e.getMessage());
                            return assignCaseAccessFailure(caseDetails, emptyList());
                        }
                    } else {
                        String errorMessage = "Applicant solicitor does not belong to chosen applicant organisation";
                        return assignCaseAccessFailure(caseDetails, singletonList(errorMessage));
                    }
                } else {
                    String errorMessage = "Applicant organisation not selected";
                    return assignCaseAccessFailure(caseDetails, singletonList(errorMessage));
                }
            } catch (Exception e) {
                log.info("Exception when trying to assign case access for Case ID: {}, {}",
                    caseDetails.getId(), e.getMessage());
                return assignCaseAccessFailure(caseDetails, emptyList());
            }
        } else {
            log.info("Applicant Policy Already Applied, Case ID: {}", caseDetails.getId());
        }

        final Map<String, Object> mapOfCaseData = caseDetails.getData();
        mapOfCaseData.put(SUBMIT_CASE_DATE, LocalDate.now());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(mapOfCaseData).build());
    }

    private String getApplicantOrgId(CaseDetails caseDetails) {
        Map<String, Object> applicantOrgPolicy = (Map<String, Object>) caseDetails.getData().get(ORGANISATION_POLICY_APPLICANT);
        if (applicantOrgPolicy != null) {
            Map<String, Object> applicantOrganisation = (Map<String, Object>) applicantOrgPolicy.get(ORGANISATION_POLICY_ORGANISATION);

            if (applicantOrganisation != null) {
                return (String) applicantOrganisation.get(ORGANISATION_POLICY_ORGANISATION_ID);
            }
        }

        return null;
    }

    private String getRespondentOrgId(CaseDetails caseDetails) {
        Map<String, Object> respondentOrgPolicy = (Map<String, Object>) caseDetails.getData().get(ORGANISATION_POLICY_RESPONDENT);
        if (respondentOrgPolicy != null) {
            Map<String, Object> respondentOrganisation = (Map<String, Object>) respondentOrgPolicy.get(ORGANISATION_POLICY_ORGANISATION);

            if (respondentOrganisation != null) {
                return (String) respondentOrganisation.get(ORGANISATION_POLICY_ORGANISATION_ID);
            }
        }

        return null;
    }

    private boolean isRespondentRepresented(CaseDetails caseDetails) {
        return (YesOrNo.isYes(caseDetails.getData().get(CONSENTED_RESPONDENT_REPRESENTED).toString()));
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> assignCaseAccessFailure(CaseDetails caseDetails, List<String> errorDetails) {
        log.info("Assigning case access failed for Case ID: {}", caseDetails.getId());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .addAll(errorDetails != null ? errorDetails : emptyList())
                .add("Failed to assign applicant solicitor to case, please ensure you have selected the correct applicant organisation on case")
                .build())
            .build());
    }

    private ResponseEntity<AboutToStartOrSubmitCallbackResponse> miamInvalidLegacyExemptionsFailure(Map<String, Object> caseData) {
        List<String> invalidLegacyExemptions = miamLegacyExemptionsService.getInvalidLegacyExemptions(caseData);

        List<String> errors = new ArrayList<>();
        errors.add(MIAM_INVALID_LEGACY_EXEMPTIONS_ERROR_MESSAGE);
        errors.addAll(invalidLegacyExemptions);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData)
            .errors(errors)
            .build());
    }
}
