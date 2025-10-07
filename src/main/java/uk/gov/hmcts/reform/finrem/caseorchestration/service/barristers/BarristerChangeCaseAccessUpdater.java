package uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
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

        updateRepresentationUpdateHistoryForCase(caseDetails, barristerChange, authToken);
    }

    private void executeBarristerChange(FinremCaseDetails caseDetails, BarristerChange barristerChange) {
        CaseRole caseRole = manageBarristerService.getBarristerCaseRole(barristerChange.getBarristerParty());
        barristerChange.getAdded()
            .forEach(barristerToAdd -> addUserToCase(caseDetails.getId(), caseRole, barristerToAdd));
        barristerChange.getRemoved()
            .forEach(barristerToRemove -> removeUserFromCase(caseDetails.getId(), caseRole, barristerToRemove));
    }

    private void addUserToCase(long caseId, CaseRole caseRole, Barrister barristerToAdd) {
        String orgId = barristerToAdd.getOrganisation().getOrganisationID();
        assignCaseAccessService.grantCaseRoleToUser(caseId, barristerToAdd.getUserId(), caseRole.getCcdCode(), orgId);
    }

    private void removeUserFromCase(long caseId, CaseRole caseRole, Barrister barristerToRemove) {
        Optional<String> userId = Optional.ofNullable(barristerToRemove.getUserId());
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

    private void updateRepresentationUpdateHistoryForCase(FinremCaseDetails caseDetails, BarristerChange barristerChange,
                                                          String authToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<RepresentationUpdateHistoryCollection> representationUpdateHistory =
            Optional.ofNullable(caseData.getRepresentationUpdateHistory())
                .orElse(new ArrayList<>());

        barristerChange.getAdded().forEach(addedUser -> representationUpdateHistory.add(
            RepresentationUpdateHistoryCollection.builder()
                .id(UUID.randomUUID())
                .value(representationUpdateBuilder.buildBarristerAdded(getBarristerUpdateParams(caseData, authToken,
                    barristerChange.getBarristerParty(), addedUser)))
                .build()));
        barristerChange.getRemoved().forEach(removedUser -> representationUpdateHistory.add(
            RepresentationUpdateHistoryCollection.builder()
                .id(UUID.randomUUID())
                .value(representationUpdateBuilder.buildBarristerRemoved(getBarristerUpdateParams(caseData, authToken,
                    barristerChange.getBarristerParty(), removedUser)))
                .build()));
        caseData.setRepresentationUpdateHistory(representationUpdateHistory);
    }

    private BarristerRepresentationUpdateBuilder.BarristerUpdateParams getBarristerUpdateParams(FinremCaseData caseData,
                                                                                                String authToken,
                                                                                                BarristerParty barristerParty,
                                                                                                Barrister barrister) {
        return new BarristerRepresentationUpdateBuilder.BarristerUpdateParams(caseData, authToken, barristerParty, barrister);
    }
}
