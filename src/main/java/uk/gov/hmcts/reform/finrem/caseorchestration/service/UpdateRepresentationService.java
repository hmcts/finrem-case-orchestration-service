package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.events.AuditEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private final AuditEventService auditEventService;
    private final IdamAuthService idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataService caseDataService;
    private final PrdOrganisationService organisationService;
    private final UpdateSolicitorDetailsService updateSolicitorDetailsService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final AssignCaseAccessService assignCaseAccessService;

    private static final String CHANGE_REQUEST_FIELD = "changeOrganisationRequestField";
    private static final String NOC_EVENT = "nocRequest";
    private static final String CHANGE_OF_REPRESENTATIVES = "ChangeOfRepresentatives";
    private static final String REMOVE_REPRESENTATION_EVENT = "removeRepresentation";


    private boolean isApplicant;

    public Map<String, Object> updateRepresentation(CaseDetails caseDetails,
                                                               String authToken)  {

        log.info("Updating representation for case ID {}", caseDetails.getId());

        AuditEvent auditEvent = auditEventService.getLatestNocAuditEventByName(caseDetails.getId().toString())
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find %s or %s event in audit",
                NOC_EVENT,
                REMOVE_REPRESENTATION_EVENT)));

        final UserDetails invoker = idamClient.getUserByUserId(authToken, auditEvent.getUserId());

        return auditEvent.getId().equals(NOC_EVENT)
            ? addOrReplaceRepresentation(caseDetails, invoker)
            : revokeSolicitorAccess(caseDetails, invoker);
    }

    private Map<String, Object> addOrReplaceRepresentation(CaseDetails caseDetails,
                                                           UserDetails invoker) {
        final ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);
        isApplicant = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);

        final ChangedRepresentative addedSolicitor = getAddedSolicitor(invoker, changeRequest);
        final ChangedRepresentative removedSolicitor = getRemovedSolicitor(caseDetails, changeRequest);

        log.info("About to start updating solicitor details in the case data for caseId: {}", caseDetails.getId());
        caseDetails.getData().putAll(updateCaseDataWithNewSolDetails(caseDetails, addedSolicitor));

        return updateChangeOfRepresentatives(caseDetails, addedSolicitor, removedSolicitor, invoker.getFullName());
    }

    private Map<String, Object> revokeSolicitorAccess(CaseDetails caseDetails,
                                                      UserDetails invoker) {
        ChangeOrganisationRequest changeRequest = getChangeOrganisationRequest(caseDetails);
        isApplicant = changeRequest.getCaseRoleId().getValueCode().equals(APP_SOLICITOR_POLICY);
        log.info("About to start revoking solicitor case access");

        CaseAssignmentUserRolesResponse ccdResponse = revokeAccessForSolicitor(changeRequest, caseDetails);

        log.info("ccd response when revoking access: {}", ccdResponse);
        updateLitigantDetails(caseDetails);
        ChangedRepresentative removedSolicitor = getRemovedSolicitor(caseDetails, changeRequest);
        return updateChangeOfRepresentatives(caseDetails, null, removedSolicitor, invoker.getFullName());
    }

    private Map<String, Object> updateChangeOfRepresentatives(CaseDetails caseDetails,
                                                              ChangedRepresentative addedSolicitor,
                                                              ChangedRepresentative removedSolicitor,
                                                              String invoker) {

        Map<String, Object> caseData = caseDetails.getData();
        ChangeOfRepresentatives current = getCurrentChangeOfRepresentatives(caseData);

        ChangeOfRepresentatives change = changeOfRepresentationService
            .generateChangeOfRepresentatives(buildChangeOfRepresentationRequest(caseDetails,
                addedSolicitor,
                removedSolicitor,
                current,
                invoker));

        caseData.put(CHANGE_OF_REPRESENTATIVES, change.getChangeOfRepresentation());

        return caseData;
    }

    private Map<String, Object> updateCaseDataWithNewSolDetails(CaseDetails caseDetails,
                                                                ChangedRepresentative addedSolicitor) {

        Map<String, Object> caseData = caseDetails.getData();
        boolean isConsented = caseDataService.isConsentedApplication(caseDetails);
        String appSolicitorAddressField = isConsented ? CONSENTED_SOLICITOR_ADDRESS : CONTESTED_SOLICITOR_ADDRESS;
        String solicitorAddressField = isApplicant ? appSolicitorAddressField : RESP_SOLICITOR_ADDRESS;

        caseData.put(isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails), YES_VALUE);
        OrganisationsResponse organisationsResponse = organisationService
            .findOrganisationByOrgId(addedSolicitor.getOrganisation().getOrganisationID());

        caseData.put(solicitorAddressField,
            updateSolicitorDetailsService.convertOrganisationAddressToSolicitorAddress(organisationsResponse));

        Map<String, Object> updatedCaseData = updateSolicitorDetailsService.updateSolicitorContactDetails(addedSolicitor,
            caseData, isConsented, isApplicant);

        return updateSolicitorDetailsService.removeSolicitorFields(updatedCaseData, isConsented, isApplicant);
    }

    private void updateLitigantDetails(CaseDetails caseDetails) {
        caseDetails.getData().put(isApplicant ? APPLICANT_REPRESENTED
            : getRespondentRepresentedKey(caseDetails), NO_VALUE);

        if (isApplicant) {
            updateSolicitorDetailsService.removeAppSolicitorContactDetails(caseDetails);
        } else {
            updateSolicitorDetailsService.removeRespSolicitorContactDetails(caseDetails);
        }
    }

    private ChangeOrganisationRequest getChangeOrganisationRequest(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData().get(CHANGE_REQUEST_FIELD),
            ChangeOrganisationRequest.class);
    }

    private String getApplicantSolicitorName(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? (String) caseData.get(CONSENTED_SOLICITOR_NAME) : (String) caseData.get(CONTESTED_SOLICITOR_NAME);
    }

    private String getApplicantSolicitorEmail(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseDataService.isConsentedApplication(caseDetails)
            ? (String) caseData.get(SOLICITOR_EMAIL) : (String) caseData.get(CONTESTED_SOLICITOR_EMAIL);
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }

    private ChangedRepresentative getAddedSolicitor(UserDetails solicitorToAdd, ChangeOrganisationRequest changeRequest) {
        return ChangedRepresentative.builder()
            .name(solicitorToAdd.getFullName())
            .email(solicitorToAdd.getEmail())
            .organisation(changeRequest.getOrganisationToAdd())
            .build();
    }

    private ChangedRepresentative getRemovedSolicitor(CaseDetails caseDetails, ChangeOrganisationRequest changeRequest) {
        return Optional.ofNullable(changeRequest.getOrganisationToRemove())
            .map(org -> ChangedRepresentative.builder()
                .name(isApplicant ? getApplicantSolicitorName(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_NAME))
                .email(isApplicant ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL))
                .organisation(org).build())
            .orElse(null);
    }

    private ChangeOfRepresentatives getCurrentChangeOfRepresentatives(Map<String, Object> caseData) {
        return ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPRESENTATIVES),
                new TypeReference<>() {})).build();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(CaseDetails caseDetails,
                                                                             ChangedRepresentative addedSolicitor,
                                                                             ChangedRepresentative removedSolicitor,
                                                                             ChangeOfRepresentatives current,
                                                                             String invokerName) {
        return ChangeOfRepresentationRequest.builder()
            .by(invokerName)
            .party(isApplicant ? "Applicant" : "Respondent")
            .clientName(isApplicant ? caseDataService.buildFullApplicantName(caseDetails)
                : caseDataService.buildFullRespondentName(caseDetails))
            .current(current)
            .addedRepresentative(addedSolicitor)
            .removedRepresentative(removedSolicitor)
            .build();
    }

    private CaseAssignmentUserRolesResponse revokeAccessForSolicitor(ChangeOrganisationRequest changeRequest,
                                                                     CaseDetails caseDetails) {

        List<CaseAssignmentUserRole> users = assignCaseAccessService
            .getUsersWithAccess(changeRequest.getCaseRoleId().getValueCode()).getCaseAssignmentUserRoles();

        return Optional.ofNullable(users.get(0))
            .map(user -> assignCaseAccessService.revokeUserAccess(
                CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRolesWithOrganisation(
                        buildCaseAssignmentRolesList(caseDetails, changeRequest, user))
                    .build()))
            .orElseThrow(() -> new IllegalStateException("Null response from ccd"));
    }

    private List<CaseAssignmentUserRoleWithOrganisation> buildCaseAssignmentRolesList(CaseDetails caseDetails,
                                                                               ChangeOrganisationRequest changeRequest,
                                                                               CaseAssignmentUserRole user) {
        return List.of(CaseAssignmentUserRoleWithOrganisation.builder()
            .caseDataId(caseDetails.getId().toString())
            .caseRole(changeRequest.getCaseRoleId().getValueCode())
            .organisationId(changeRequest.getOrganisationToRemove().getOrganisationID())
            .userId(user.getUserId())
            .build());
    }
}
