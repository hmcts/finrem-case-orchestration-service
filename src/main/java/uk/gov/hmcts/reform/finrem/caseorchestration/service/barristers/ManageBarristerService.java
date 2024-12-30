package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NoSuchUserException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.BarristerUpdateDifferenceCalculator;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerLetterTuple;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.ADDED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChangeType.REMOVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_BARRISTER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_1_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_1_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_2_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_2_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_3_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_3_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_4_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER_BARRISTER_4_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_1_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_2_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_3_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTVR_SOLICITOR_4_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANAGE_BARRISTERS;
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

    private final AssignCaseAccessService assignCaseAccessService;
    private final BarristerLetterService barristerLetterService;
    private final BarristerUpdateDifferenceCalculator barristerUpdateDifferenceCalculator;
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final CaseDataService caseDataService;
    private final IdamService idamService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final PrdOrganisationService organisationService;
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
        barristerChange.getRemoved().forEach(userToBeRemoved -> removeUser(caseDetails, authToken, caseRole, userToBeRemoved));

        return updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, authToken);
    }

    public void notifyBarristerAccess(CaseDetails caseDetails,
                                      List<Barrister> barristers,
                                      List<Barrister> barristersBeforeEvent,
                                      String authToken) {
        BarristerChange barristerChange =
            barristerUpdateDifferenceCalculator.calculate(barristersBeforeEvent, barristers);
        List<Barrister> addedBarristers = barristerChange.getAdded().stream().toList();
        List<Barrister> removedBarristers = barristerChange.getRemoved().stream().toList();

        addedBarristers.forEach(barrister ->
            sendNotifications(caseDetails, barrister, Pair.of(authToken, ADDED), authToken));
        removedBarristers.forEach(barrister ->
            sendNotifications(caseDetails, barrister, Pair.of(authToken, REMOVED), authToken));
    }

    private void sendNotifications(CaseDetails caseDetails,
                                   Barrister barrister,
                                   Pair<String, BarristerChangeType> letterData,
                                   String authToken) {
        if (letterData.getRight().equals(ADDED)) {
            notificationService.sendBarristerAddedEmail(caseDetails, barrister);
        } else {
            notificationService.sendBarristerRemovedEmail(caseDetails, barrister);
        }

        DocumentHelper.PaperNotificationRecipient recipient = getPaperNotificationRecipient(caseDetails, letterData);
        barristerLetterService.sendBarristerLetter(
            caseDetails,
            barrister,
            BarristerLetterTuple.of(letterData, recipient),
            authToken);
    }

    public String getCaseRole(CaseDetails caseDetails, String authToken) {
        CaseAssignedUserRolesResource caseRoleResource =
            caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), authToken);
        log.info("Case assigned role resource is: {}", caseRoleResource.toString());
        String caseRole = isCaseRoleResourceNullOrEmpty(caseRoleResource)
            ? CASEWORKER_ROLE
            : caseRoleResource.getCaseAssignedUserRoles().get(0).getCaseRole();

        if (!List.of(APP_SOLICITOR_POLICY, RESP_SOLICITOR_POLICY, INTVR_SOLICITOR_1_POLICY, INTVR_SOLICITOR_2_POLICY,
            INTVR_SOLICITOR_3_POLICY, INTVR_SOLICITOR_4_POLICY).contains(caseRole)) {
            String caseworkerParty = Objects.toString(caseDetails.getData().get(MANAGE_BARRISTER_PARTY), StringUtils.EMPTY);
            log.info("caseWorker party is {}", caseworkerParty);
            if (APPLICANT.equalsIgnoreCase(caseworkerParty)) {
                caseRole = APP_SOLICITOR_POLICY;
            } else if (
                RESPONDENT.equalsIgnoreCase(caseworkerParty)) {
                caseRole = RESP_SOLICITOR_POLICY;
            } else if (
                INTERVENER1.equalsIgnoreCase(caseworkerParty)) {
                caseRole = INTVR_SOLICITOR_1_POLICY;
            } else if (
                INTERVENER2.equalsIgnoreCase(caseworkerParty)) {
                caseRole = INTVR_SOLICITOR_2_POLICY;
            } else if (
                INTERVENER3.equalsIgnoreCase(caseworkerParty)) {
                caseRole = INTVR_SOLICITOR_3_POLICY;
            } else if (
                INTERVENER4.equalsIgnoreCase(caseworkerParty)) {
                caseRole = INTVR_SOLICITOR_4_POLICY;
            }
        }
        log.info("Case role is {}", caseRole);
        return caseRole;
    }

    /*Below is temporary solution while ref data has code freeze - findUserByEmail endpoint is secured
    Finrem's system user has required roles, so below can be permanent solution but not preferred
    TODO: create Ref Data (RDCC) Jira ticket to add finrem's caseworker to list of secured roles.*/
    public String getAuthTokenToUse(CaseDetails caseDetails, String authToken) {
        String caseworkerParty = Objects.toString(caseDetails.getData().get(MANAGE_BARRISTER_PARTY), StringUtils.EMPTY);
        return caseworkerParty.isEmpty() ? authToken : systemUserService.getSysUserToken();
    }

    private void addUser(CaseDetails caseDetails, String authToken, String caseRole, Barrister userToBeAdded) {
        String orgId = userToBeAdded.getOrganisation().getOrganisationID();
        organisationService.findUserByEmail(userToBeAdded.getEmail(), getAuthTokenToUse(caseDetails, authToken))
            .ifPresentOrElse(
                userId -> assignCaseAccessService.grantCaseRoleToUser(caseDetails.getId(), userId, caseRole, orgId),
                throwNoSuchUserException()
            );
    }

    private void removeUser(CaseDetails caseDetails, String authToken, String caseRole, Barrister userToBeRemoved) {
        String orgId = userToBeRemoved.getOrganisation().getOrganisationID();
        organisationService.findUserByEmail(userToBeRemoved.getEmail(), getAuthTokenToUse(caseDetails, authToken))
            .ifPresentOrElse(
                userId -> assignCaseAccessService.removeCaseRoleToUser(caseDetails.getId(), userId, caseRole, orgId),
                throwNoSuchUserException()
            );
    }

    private Map<String, Object> updateRepresentationUpdateHistoryForCase(CaseDetails caseDetails,
                                                                         BarristerChange barristerChange,
                                                                         String authToken) {
        List<Element<RepresentationUpdate>> representationUpdateHistory = getRepresentationUpdateHistory(caseDetails);

        barristerChange.getAdded().forEach(addedUser -> representationUpdateHistory.add(
            element(UUID.randomUUID(), buildRepresentationUpdate(caseDetails, authToken, addedUser, null)))
        );

        barristerChange.getRemoved().forEach(removedUser -> representationUpdateHistory.add(
            element(UUID.randomUUID(), buildRepresentationUpdate(caseDetails, authToken, null, removedUser)))
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
            .date(LocalDateTime.now())
            .clientName(getClientName(caseDetails, authToken))
            .party(getManageBarristerParty(caseDetails, authToken))
            .build();
    }

    private boolean isCaseRoleResourceNullOrEmpty(CaseAssignedUserRolesResource caseRoleResource) {
        return caseRoleResource.getCaseAssignedUserRoles() == null || caseRoleResource.getCaseAssignedUserRoles().isEmpty();
    }

    private String getBarristerCaseRole(CaseDetails caseDetails, String authToken) {
        if (APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return APPLICANT_BARRISTER_ROLE;
        } else if (RESP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return RESPONDENT_BARRISTER_ROLE;
        } else if (INTVR_SOLICITOR_1_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return INTERVENER_BARRISTER_1_ROLE;
        } else if (INTVR_SOLICITOR_2_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return INTERVENER_BARRISTER_2_ROLE;
        } else if (INTVR_SOLICITOR_3_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return INTERVENER_BARRISTER_3_ROLE;
        } else {
            return INTERVENER_BARRISTER_4_ROLE;
        }
    }

    private String getBarristerCollectionKey(String caseRole) {
        if (APP_SOLICITOR_POLICY.equals(caseRole)) {
            return APPLICANT_BARRISTER_COLLECTION;
        } else if (RESP_SOLICITOR_POLICY.equals(caseRole)) {
            return RESPONDENT_BARRISTER_COLLECTION;
        } else if (INTVR_SOLICITOR_1_POLICY.equals(caseRole)) {
            return INTERVENER_BARRISTER_1_COLLECTION;
        } else if (INTVR_SOLICITOR_2_POLICY.equals(caseRole)) {
            return INTERVENER_BARRISTER_2_COLLECTION;
        } else if (INTVR_SOLICITOR_3_POLICY.equals(caseRole)) {
            return INTERVENER_BARRISTER_3_COLLECTION;
        } else {
            return INTERVENER_BARRISTER_4_COLLECTION;
        }
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
        if (APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return APPLICANT;
        } else if (RESP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, authToken))) {
            return RESPONDENT;
        } else {
            return INTERVENER;
        }
    }

    private DocumentHelper.PaperNotificationRecipient getPaperNotificationRecipient(CaseDetails caseDetails,
                                                                                    Pair<String, BarristerChangeType> letterData) {
        return APP_SOLICITOR_POLICY.equals(getCaseRole(caseDetails, letterData.getLeft()))
            ? DocumentHelper.PaperNotificationRecipient.APPLICANT
            : DocumentHelper.PaperNotificationRecipient.RESPONDENT;
    }

    private List<BarristerData> getBarristerCollection(CaseDetails caseDetails, String caseRole) {
        String barristerCollectionKey = getBarristerCollectionKey(caseRole);
        return objectMapper.convertValue(caseDetails.getData().get(barristerCollectionKey), new TypeReference<>() {});
    }

    private List<Element<RepresentationUpdate>> getRepresentationUpdateHistory(CaseDetails caseDetails) {
        return Optional.ofNullable(convertToUpdateHistory(caseDetails)).orElse(new ArrayList<>());
    }

    private List<Element<RepresentationUpdate>> convertToUpdateHistory(CaseDetails caseDetails) {
        return objectMapper.registerModule(new JavaTimeModule())
            .convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {});
    }

    private Runnable throwNoSuchUserException() {
        return () -> {
            throw new NoSuchUserException("Could not find barrister with provided email");
        };
    }
}
