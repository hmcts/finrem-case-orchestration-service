package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarristerChangeCaseAccessUpdater {

    private final ManageBarristerService manageBarristerService;
    private final PrdOrganisationService organisationService;
    private final SystemUserService systemUserService;
    private final AssignCaseAccessService assignCaseAccessService;
    private final BarristerRepresentationUpdateBuilder representationUpdateBuilder;

    /**
     * Updates case access for barristers being added or removed from a case and updates the representation history.
     *
     * @param caseDetails     case details
     * @param authToken       auth token
     * @param barristerChange barrister change details
     */
    public void update(FinremCaseDetails caseDetails, String authToken, BarristerChange barristerChange) {
        executeBarristerChange(caseDetails, barristerChange);

        updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, null, authToken);
    }

    /**
     * Executes barrister access changes for a case.
     *
     * <p>
     * This method determines the appropriate {@link CaseRole} for the barrister party,
     * then interacts with the Assign Case Access Service to update user access
     * on the case. All barristers listed in {@code barristerChange.getAdded()} are
     * granted access, and all barristers listed in {@code barristerChange.getRemoved()}
     * have their access removed.
     *
     * @param caseDetails      the case details containing the case ID
     * @param barristerChange  the object describing which barristers to add or remove
     */
    private void executeBarristerChange(FinremCaseDetails caseDetails, BarristerChange barristerChange) {
        executeBarristerChange(caseDetails.getId(), barristerChange);
    }

    /**
     * Executes barrister access changes for a case.
     *
     * <p>
     * This method determines the appropriate {@link CaseRole} for the barrister party,
     * then interacts with the Assign Case Access Service to update user access
     * on the case. All barristers listed in {@code barristerChange.getAdded()} are
     * granted access, and all barristers listed in {@code barristerChange.getRemoved()}
     * have their access removed.
     *
     * @param caseId           the case ID
     * @param barristerChange  the object describing which barristers to add or remove
     */
    public void executeBarristerChange(long caseId, BarristerChange barristerChange) {
        CaseRole caseRole = manageBarristerService.getBarristerCaseRole(barristerChange.getBarristerParty());
        barristerChange.getAdded()
            .forEach(barristerToAdd -> addUserToCase(caseId, caseRole, barristerToAdd));
        barristerChange.getRemoved()
            .forEach(barristerToRemove -> removeUserFromCase(caseId, caseRole, barristerToRemove));
    }

    private void addUserToCase(long caseId, CaseRole caseRole, Barrister barristerToAdd) {
        String orgId = barristerToAdd.getOrganisation().getOrganisationID();
        assignCaseAccessService.grantCaseRoleToUser(caseId, barristerToAdd.getUserId(), caseRole.getCcdCode(), orgId);
    }

    private void removeUserFromCase(long caseId, CaseRole caseRole, Barrister barristerToRemove) {
        Optional<String> userId = ofNullable(barristerToRemove.getUserId());
        if (userId.isEmpty()) {
            String authToken = systemUserService.getSysUserToken();
            userId = organisationService.findUserByEmail(barristerToRemove.getEmail(), authToken);
        }

        String orgId = barristerToRemove.getOrganisation().getOrganisationID();
        userId.ifPresentOrElse(
            id -> assignCaseAccessService.removeCaseRoleToUser(caseId, id, caseRole.getCcdCode(), orgId),
            () -> log.warn("Case ID {}: Could not find user to remove with organisation id {}", caseId, orgId)
        );
    }

    /**
     * Updates the representation update history for a case when barrister access changes occur.
     *
     * <p>
     * This method records each barrister added to, or removed from, the case by creating
     * new {@link RepresentationUpdateHistoryCollection} entries. It uses the details in
     * {@code barristerChange} to determine which users were added or removed, builds the
     * corresponding history records, and appends them to the case's existing update history.
     * The {@code viaEventType} value indicates which event triggered this update. The final
     * updated history is then saved back to the case data.
     *
     * @param caseDetails     the case containing the current case data
     * @param barristerChange the details of barristers added to or removed from the case
     * @param viaEventType    the event type that triggered this update
     * @param authToken       the authorisation token used to fetch user and case information
     */
    public void updateRepresentationUpdateHistoryForCase(FinremCaseDetails caseDetails, BarristerChange barristerChange,
                                                         EventType viaEventType, String authToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<RepresentationUpdateHistoryCollection> representationUpdateHistory =
            ofNullable(caseData.getRepresentationUpdateHistory())
                .orElse(new ArrayList<>());

        barristerChange.getAdded().forEach(addedUser -> representationUpdateHistory.add(
            RepresentationUpdateHistoryCollection.builder()
                .id(UUID.randomUUID())
                .value(representationUpdateBuilder.buildBarristerAdded(getBarristerUpdateParams(caseData, authToken,
                    barristerChange.getBarristerParty(), addedUser, viaEventType)))
                .build()));
        barristerChange.getRemoved().forEach(removedUser -> representationUpdateHistory.add(
            RepresentationUpdateHistoryCollection.builder()
                .id(UUID.randomUUID())
                .value(representationUpdateBuilder.buildBarristerRemoved(getBarristerUpdateParams(caseData, authToken,
                    barristerChange.getBarristerParty(), removedUser, viaEventType)))
                .build()));
        caseData.setRepresentationUpdateHistory(representationUpdateHistory);
    }

    private BarristerRepresentationUpdateBuilder.BarristerUpdateParams getBarristerUpdateParams(FinremCaseData caseData,
                                                                                                String authToken,
                                                                                                BarristerParty barristerParty,
                                                                                                Barrister barrister,
                                                                                                EventType viaEventType) {
        return new BarristerRepresentationUpdateBuilder.BarristerUpdateParams(caseData, authToken,
            barristerParty, barrister, ofNullable(viaEventType).map(this::describeVia).orElse(null));
    }

    private String describeVia(EventType viaEventType) {
        return switch (viaEventType) {
            case MANAGE_BARRISTER -> CCDConfigConstant.MANAGE_BARRISTERS;
            case STOP_REPRESENTING_CLIENT -> "Stop representing a client";
            default -> viaEventType.name();
        };
    }
}
