package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManageBarristerService {

    public static final String MANAGE_BARRISTER_PARTY = "barristerParty";
    public static final String MANAGE_BARRISTERS = "Manage Barristers";
    public static final String CASEWORKER_ROLE = "[CASEWORKER]";

    private final BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final NotificationService notificationService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;

    public List<BarristerData> getBarristersForParty(CaseDetails caseDetails, String authToken) {
        String caseRole = getCaseRole(caseDetails, authToken);

        log.info("Fetched case role with value {} for case {}", caseRole, caseDetails.getId());
        return Optional.ofNullable(getBarristerCollection(caseDetails, caseRole)).orElse(new ArrayList<>());
    }

    public Map<String, Object> updateBarristerAccess(CaseDetails caseDetails,
                                                     List<Barrister> barristers,
                                                     List<Barrister> barristersBeforeEvent,
                                                     String authToken) {
        log.info("About to start updating barrister access for case {}", caseDetails.getId());
        final BarristerChange barristerChange = barristerUpdateDifferenceCalculator.calculate(barristersBeforeEvent, barristers);
        final String caseRole = getBarristerCaseRole(caseDetails, authToken);

        log.info("changed barristers: {}", barristerChange.toString());
        barristerChange.getAdded().forEach(userToBeAdded -> addUser(caseDetails, authToken, caseRole, userToBeAdded));

        return updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, authToken);
    }

    public void notifyBarristerAccess(CaseDetails caseDetails,
                                                     List<Barrister> barristers,
                                                     List<Barrister> barristersBeforeEvent) {

        BarristerChange barristerChange = barristerUpdateDifferenceCalculator.calculate(barristers, barristersBeforeEvent);
        List<Barrister> addedBarristers = barristerChange.getAdded().stream().toList();
        List<Barrister> removedBarristers = barristerChange.getRemoved().stream().toList();

        addedBarristers.forEach(barrister -> notificationService.sendBarristerAddedEmail(caseDetails, barrister));
        removedBarristers.forEach(barrister -> notificationService.sendBarristerRemovedEmail(caseDetails, barrister));

    }

    private void addUser(CaseDetails caseDetails, String authToken, String caseRole, Barrister userToBeAdded) {
        String orgId = userToBeAdded.getOrganisation().getOrganisationID();
        organisationService.findUserByEmail(userToBeAdded.getEmail(), authToken)
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseDetails.getId(), userId, caseRole, orgId),
                throwMissingUserException(userToBeAdded)
            );
    }

    private Map<String, Object> updateRepresentationUpdateHistoryForCase(CaseDetails caseDetails,
                                                                         BarristerChange barristerChange,
                                                                         String authToken) {
        barristerChange.getAdded().forEach(addedUser -> {
            RepresentationUpdateHistory representationUpdateHistory =
                changeOfRepresentationService.generateRepresentationUpdateHistory(
                    buildChangeOfRepresentationRequest(caseDetails, authToken, addedUser));
            caseDetails.getData().put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory.getRepresentationUpdateHistory());
        });

        return caseDetails.getData();
    }

    private ChangeOfRepresentationRequest buildChangeOfRepresentationRequest(CaseDetails caseDetails,
                                                                             String authToken,
                                                                             Barrister addedUser) {
        return ChangeOfRepresentationRequest.builder()
            .addedRepresentative(convertToChangedRepresentative(addedUser))
            .by(idamService.getIdamFullName(authToken))
            .via(MANAGE_BARRISTERS)
            .clientName(getClientName(caseDetails, authToken))
            .party(getManageBarristerParty(caseDetails, authToken))
            .current(getRepresentationUpdateHistory(caseDetails))
            .build();
    }

    public String getCaseRole(CaseDetails caseDetails, String authToken) {
        CaseAssignedUserRolesResource caseRoleResource = caseAssignedRoleService.getCaseAssignedUserRole(caseDetails, authToken);
        String caseRole = isCaseRoleResourceNullOrEmpty(caseRoleResource)
            ? CASEWORKER_ROLE
            : caseRoleResource.getCaseAssignedUserRoles().get(0).getCaseRole();

        if (!List.of(APP_SOLICITOR_POLICY, RESP_SOLICITOR_POLICY).contains(caseRole)) {
            String caseworkerParty = Objects.toString(caseDetails.getData().get(MANAGE_BARRISTER_PARTY), StringUtils.EMPTY);
            caseRole = APPLICANT.equalsIgnoreCase(caseworkerParty) ? APP_SOLICITOR_POLICY : RESP_SOLICITOR_POLICY;
        }

        return caseRole;
    }

    private boolean isCaseRoleResourceNullOrEmpty(CaseAssignedUserRolesResource caseRoleResource) {
        return caseRoleResource.getCaseAssignedUserRoles() == null || caseRoleResource.getCaseAssignedUserRoles().isEmpty();
    }

    private String getBarristerCaseRole(CaseDetails caseDetails, String authToken) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken)) ? APPLICANT_BARRISTER_ROLE : RESPONDENT_BARRISTER_ROLE;
    }

    private String getBarristerCollectionKey(String caseRole) {
        return APP_SOLICITOR_POLICY.equals(caseRole) ? APPLICANT_BARRISTER_COLLECTION : RESPONDENT_BARRISTER_COLLECTION;
    }

    private ChangedRepresentative convertToChangedRepresentative(Barrister barrister) {
        return ChangedRepresentative.builder()
            .name(barrister.getName())
            .email(barrister.getEmail())
            .organisation(barrister.getOrganisation())
            .build();
    }

    private RepresentationUpdateHistory getRepresentationUpdateHistory(CaseDetails caseDetails) {
        return RepresentationUpdateHistory.builder()
            .representationUpdateHistory(Optional.ofNullable(getUpdateHistory(caseDetails)).orElse(new ArrayList<>()))
            .build();
    }

    private String getClientName(CaseDetails caseDetails, String authToken) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken))
            ? caseDataService.buildFullApplicantName(caseDetails)
            : caseDataService.buildFullRespondentName(caseDetails);
    }

    private String getManageBarristerParty(CaseDetails caseDetails, String authToken) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken)) ? APPLICANT : RESPONDENT;
    }

    private List<BarristerData> getBarristerCollection(CaseDetails caseDetails, String caseRole) {
        String barristerCollectionKey = getBarristerCollectionKey(caseRole);
        return objectMapper.convertValue(caseDetails.getData().get(barristerCollectionKey), new TypeReference<>() {});
    }

    private List<Element<RepresentationUpdate>> getUpdateHistory(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
    }

    private Runnable throwMissingUserException(Barrister userToBeAdded) {
        return () -> {
            throw new IllegalArgumentException(String.format("Could not find the user with email %s",
                userToBeAdded.getEmail()));
        };
    }
}
