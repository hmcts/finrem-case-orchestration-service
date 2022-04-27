package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentatives;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {
    private static final String APPLICANT = "Applicant";
    private static final String RESPONDENT = "Respondent";
    private static final String CHANGE_OF_REPS = "ChangeOfRepresentatives";
    private static final String ADDING = "ADDING";
    private static final String REPLACING = "REPLACING";
    private static final String REMOVING = "REMOVING";

    private final CaseDataService caseDataService;
    private final IdamService idamService;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final ObjectMapper objectMapper;
    private final AssignCaseAccessService assignCaseAccessService;
    private boolean isApplicant;

    @Autowired
    public NoticeOfChangeService(CaseDataService caseDataService,
                                 IdamService idamService,
                                 ChangeOfRepresentationService changeOfRepresentationService,
                                 AssignCaseAccessService assignCaseAccessService) {
        this.caseDataService = caseDataService;
        this.idamService = idamService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.changeOfRepresentationService = changeOfRepresentationService;
        this.assignCaseAccessService = assignCaseAccessService;
    }

    public Map<String, Object> caseWorkerUpdatesRepresentation(CaseDetails caseDetails,
                                                               String authorizationToken,
                                                               CaseDetails originalCaseDetails) {
        Map<String,Object> caseData = caseDetails.getData();
        isApplicant = ((String) caseData.get(NOC_PARTY)).equalsIgnoreCase(APPLICANT);
        log.info("case details for caseID {}: ", caseDetails);
        final OrganisationPolicy orgPolicyToUpdate = getOrganisationPolicy(caseDetails);
        log.info("About to start noc as caseworker for caseID {}", caseDetails.getId());
        log.info("Org policy before update: {}", getOriginalOrgPolicy(originalCaseDetails));
        log.info("Org policy after update: {}", getOrganisationPolicy(caseDetails));

        caseData = updateChangeOfRepresentatives(caseDetails,
            authorizationToken,
            originalCaseDetails,
            orgPolicyToUpdate);

        ChangeOfRepresentatives changeOfRepresentatives = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPS),
                new TypeReference<>() {}))
            .build();

        final String natureOfChange = getNatureOfChange(changeOfRepresentatives);

        updateOrganisationPolicy(orgPolicyToUpdate, caseData, natureOfChange);

        if (REPLACING.equals(natureOfChange) || REMOVING.equals(natureOfChange)) {
            caseDetails.getData().putAll(caseData);
            revokeSolicitorAccess(caseDetails, getOrganisationPolicy(originalCaseDetails));
        }

        return caseData;
    }

    private void revokeSolicitorAccess(CaseDetails caseDetails,
                                       OrganisationPolicy organisationPolicy) {

        log.info("About to start revoking solicitor case access");
        CaseAssignmentUserRolesResponse ccdResponse = revokeAccessForSolicitor(organisationPolicy, caseDetails);
        log.info("ccd response when revoking access: {}", ccdResponse);
    }

    private void updateOrganisationPolicy(OrganisationPolicy organisationPolicy,
                                          Map<String, Object> caseData,
                                          String natureOfChange) {

        OrganisationPolicy newPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY)
            .organisation(REMOVING.equals(natureOfChange) ? null : organisationPolicy.getOrganisation())
            .build();
        caseData.put(isApplicant ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY, newPolicy);
    }

    private Map<String, Object> updateChangeOfRepresentatives(CaseDetails caseDetails,
                                                              String authToken,
                                                              CaseDetails originalDetails,
                                                              OrganisationPolicy organisationPolicy) {
        Map<String, Object> caseData = caseDetails.getData();

        ChangeOfRepresentatives current = ChangeOfRepresentatives.builder()
            .changeOfRepresentation(objectMapper.convertValue(caseData.get(CHANGE_OF_REPS),
                new TypeReference<>() {}))
            .build();

        ChangeOfRepresentatives change = changeOfRepresentationService.generateChangeOfRepresentatives(
            ChangeOfRepresentationRequest.builder()
                .by(idamService.getIdamFullName(authToken))
                .party(isApplicant ? APPLICANT : RESPONDENT)
                .clientName(isApplicant ? caseDataService.buildFullApplicantName(caseDetails)
                    : caseDataService.buildFullRespondentName(caseDetails))
                .current(current)
                .addedRepresentative(getAddedRep(caseDetails, organisationPolicy))
                .removedRepresentative(getRemovedRepresentative(originalDetails, getOrganisationPolicy(originalDetails)))
                .build()
        );

        caseData.put(CHANGE_OF_REPS, change.getChangeOfRepresentation());
        return caseData;
    }

    private ChangedRepresentative getRemovedRepresentative(CaseDetails caseDetails,
                                                           OrganisationPolicy organisationPolicy) {
        return Optional.ofNullable(organisationPolicy.getOrganisation()).map(
            organisation -> ChangedRepresentative.builder()
                .name(isApplicant ? getApplicantSolicitorName(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_NAME))
                .email(isApplicant ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL))
                .organisation(organisation)
                .build())
            .orElse(null);
    }

    private ChangedRepresentative getAddedRep(CaseDetails caseDetails, OrganisationPolicy policy) {
        Map<String, Object> caseData = caseDetails.getData();
        return Optional.ofNullable(policy.getOrganisation())
            .map(org -> ChangedRepresentative.builder()
                .name(isApplicant ? getApplicantSolicitorName(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_NAME))
                .email(isApplicant ? getApplicantSolicitorEmail(caseDetails)
                    : (String) caseData.get(RESP_SOLICITOR_EMAIL))
                .organisation(org)
                .build()).orElse(null);
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

    private String getNatureOfChange(ChangeOfRepresentatives changeOfRepresentatives) {
        Optional<Element<ChangeOfRepresentation>> changeOfRepresentationElement = changeOfRepresentatives
            .getChangeOfRepresentation()
            .stream()
            .max(Comparator.comparing(element -> element.getValue().getDate()));
        System.out.println(changeOfRepresentationElement);
        if (changeOfRepresentationElement.isPresent()) {
            ChangeOfRepresentation changeOfRepresentation = changeOfRepresentationElement.get().getValue();
            if (Optional.ofNullable(changeOfRepresentation.getAdded()).isPresent()
                && Optional.ofNullable(changeOfRepresentation.getRemoved()).isPresent()) {
                return REPLACING;
            }
            if (Optional.ofNullable(changeOfRepresentation.getAdded()).isPresent()) {
                return ADDING;
            }
            return REMOVING;
        }
        throw new IllegalStateException("Representation Change is empty or missing both added and removed fields");
    }

    private CaseAssignmentUserRolesResponse revokeAccessForSolicitor(OrganisationPolicy organisationPolicy,
                                                                     CaseDetails caseDetails) {

        List<CaseAssignmentUserRole> users = assignCaseAccessService
            .getUsersWithAccess(organisationPolicy.getOrgPolicyCaseAssignedRole())
            .getCaseAssignmentUserRoles();

        return Optional.ofNullable(users.get(0))
            .map(user -> assignCaseAccessService.revokeUserAccess(
                CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRolesWithOrganisation(
                        buildCaseAssignmentRolesList(caseDetails, organisationPolicy, user))
                    .build()))
            .orElseThrow(() -> new IllegalStateException("Null response from ccd"));
    }

    private List<CaseAssignmentUserRoleWithOrganisation> buildCaseAssignmentRolesList(CaseDetails caseDetails,
                                                                                      OrganisationPolicy organisationPolicy,
                                                                                      CaseAssignmentUserRole user) {
        return List.of(CaseAssignmentUserRoleWithOrganisation.builder()
            .caseDataId(caseDetails.getId().toString())
            .caseRole(isApplicant ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY)
            .organisationId(organisationPolicy.getOrganisation().getOrganisationID())
            .userId(user.getUserId())
            .build());
    }

    private OrganisationPolicy getOrganisationPolicy(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails.getData().get((
            (String) caseDetails.getData().get(NOC_PARTY)).equalsIgnoreCase(APPLICANT)
            ? APPLICANT_ORGANISATION_POLICY : RESPONDENT_ORGANISATION_POLICY),
            new TypeReference<>() {});
    }

    private OrganisationPolicy getOriginalOrgPolicy(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails.getData().get(APPLICANT_ORGANISATION_POLICY),
            new TypeReference<>() {});
    }

}
