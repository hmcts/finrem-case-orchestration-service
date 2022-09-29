package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTER_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManageBarristerService {

    public static final String MANAGE_BARRISTERS = "Manage Barristers";

    private final BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final AssignCaseAccessService assignCaseAccessService;
    private final PrdOrganisationService organisationService;
    private final IdamService idamService;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;

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
        caseDetails.getData().remove(MANAGE_BARRISTER_PARTY);

        return updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, authToken);
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

    /*Below is temporary solution while ref data has code freeze - findUserByEmail endpoint is secured
    Finrem's system user has required roles, so below can be permanent solution but not preferred
    Todo: create Ref Data (RDCC) Jira ticket to add finrem's caseworker to list of secured roles.*/
    public String getAuthTokenToUse(CaseDetails caseDetails, String authToken) {
        String caseworkerParty = Objects.toString(caseDetails.getData().get(MANAGE_BARRISTER_PARTY), StringUtils.EMPTY);
        return caseworkerParty.isEmpty() ? authToken : systemUserService.getSysUserToken();
    }

    private void addUser(CaseDetails caseDetails, String authToken, String caseRole, Barrister userToBeAdded) {
        String orgId = userToBeAdded.getOrganisation().getOrganisationID();
        organisationService.findUserByEmail(userToBeAdded.getEmail(), getAuthTokenToUse(caseDetails, authToken))
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseDetails.getId(), userId, caseRole, orgId),
                throwNoSuchUserException(userToBeAdded)
            );
    }

    private Map<String, Object> updateRepresentationUpdateHistoryForCase(CaseDetails caseDetails,
                                                                         BarristerChange barristerChange,
                                                                         String authToken) {
        List<Element<RepresentationUpdate>> representationUpdateHistory = getRepresentationUpdateHistory(caseDetails);

        barristerChange.getAdded().forEach(addedUser -> representationUpdateHistory.add(
            element(UUID.randomUUID(), buildRepresentationUpdate(caseDetails, authToken, addedUser, null)))
        );

        caseDetails.getData().put(REPRESENTATION_UPDATE_HISTORY, representationUpdateHistory);
        return caseDetails.getData();
    }

    private RepresentationUpdate buildRepresentationUpdate(CaseDetails caseDetails,
                                                           String authToken,
                                                           Barrister addedUser,
                                                           Barrister removedUser) {
        return RepresentationUpdate.builder()
            .added(convertToChangedRepresentative(addedUser))
            .removed(convertToChangedRepresentative(removedUser))
            .by(idamService.getIdamFullName(authToken))
            .via(MANAGE_BARRISTERS)
            .clientName(getClientName(caseDetails, authToken))
            .party(getManageBarristerParty(caseDetails, authToken))
            .build();
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
        if (barrister == null) {
            return null;
        }
        return ChangedRepresentative.builder()
            .name(barrister.getName())
            .email(barrister.getEmail())
            .organisation(barrister.getOrganisation())
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

    private List<Element<RepresentationUpdate>> getRepresentationUpdateHistory(CaseDetails caseDetails) {
        return Optional.ofNullable(getUpdateHistory(caseDetails)).orElse(new ArrayList<>());
    }

    private List<Element<RepresentationUpdate>> getUpdateHistory(CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
    }

    private Runnable throwNoSuchUserException(Barrister userToBeAdded) {
        return () -> {
            throw new NoSuchUserException(String.format("Could not find the user with email %s",
                userToBeAdded.getEmail()));
        };
    }
}
