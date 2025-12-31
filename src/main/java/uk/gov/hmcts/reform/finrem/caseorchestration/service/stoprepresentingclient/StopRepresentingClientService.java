package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientService {

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final CoreCaseDataService coreCaseDataService;

    private final IntervenerService intervenerService;

    private final CaseRoleService caseRoleService;

    private final IdamService idamService;

    private static FinremCaseData getFinremCaseDataBeforeFromInfo(StopRepresentingClientInfo info) {
        return info.getCaseDetailsBefore().getData();
    }

    private static FinremCaseData getFinremCaseDataFromInfo(StopRepresentingClientInfo info) {
        return info.getCaseDetails().getData();
    }

    private static long getCaseId(StopRepresentingClientInfo info) {
        return Long.parseLong(getFinremCaseDataFromInfo(info).getCcdCaseId());
    }

    private static Map<String, Object> clearChangeOrganisationRequestField() {
        Map<String, Object> map = new HashMap<>();
        map.put(CHANGE_ORGANISATION_REQUEST, null);
        return map;
    }

    /**
     * Applies case assignment based on who triggered the stop representing a client event.
     *
     * <p>
     * If the event is invoked by an intervener, the intervener representative request
     * is handled. Otherwise, the applicant or respondent representative request
     * is processed.
     *
     * @param info the stop representing client POJO containing the invocation context
     */
    public void applyCaseAssignment(StopRepresentingClientInfo info) {
        if (info.isInvokedByIntervener()) {
            handleIntervenerRepresentativeRequest(info);
        } else {
            handleApplicantOrRespondentRepresentativeRequest(info);
        }
    }

    private void handleApplicantOrRespondentRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final CaseType caseType = finremCaseData.getCcdCaseType();
        final long caseId = getCaseId(info);

        sendAllBarristerChangeToCaseAssignmentService(info);
        boolean isNocRequestSent = sendNocRequestToCaseAssignmentService(info);

        if (isNocRequestSent) {
            // save a call if changeOrganisationRequestField is null
            clearChangeOrganisationRequestAfterThisEvent(caseType, caseId);
        }
    }

    private boolean isIntervenerOrganisationDifference(IntervenerWrapper intervenerWrapper,
                                                       IntervenerWrapper originalIntervenerWrapper) {
        return !Objects.equals(
            intervenerWrapper.getIntervenerOrganisation(),
            originalIntervenerWrapper.getIntervenerOrganisation()
        );
    }

    private void handleIntervenerRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromInfo(info);

        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (isIntervenerOrganisationDifference(wrapper, originalWrapper)) {
                            intervenerService.revokeIntervener(info.getCaseDetails().getId(), originalWrapper);
                        }
                    });
            }
        });

        sendAllBarristerChangeToCaseAssignmentService(info);
    }

    private void sendAllBarristerChangeToCaseAssignmentService(StopRepresentingClientInfo info) {
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.APPLICANT);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.RESPONDENT);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER1);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER2);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER3);
        sendBarristerChangesToCaseAssignmentService(info, BarristerParty.INTERVENER4);
    }

    private void sendBarristerChangesToCaseAssignmentService(StopRepresentingClientInfo info, BarristerParty barristerParty) {
        final long caseId = getCaseId(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBeforeFromInfo(info);

        BarristerChange barristerChange = manageBarristerService
            .getBarristerChange(info.getCaseDetails(), finremCaseDataBefore, barristerParty);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, barristerChange);
    }

    private boolean sendNocRequestToCaseAssignmentService(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseDataFromInfo(info);
        final FinremCaseData originalFinremCaseData = getFinremCaseDataBeforeFromInfo(info);

        // to check if ChangeOrganisationRequest populated, otherwise skip it
        if (finremCaseData.getChangeOrganisationRequestField() == null) {
            log.info("{} - Not sending request to case assignment service due to changeOrganisationRequestField is null",
                finremCaseData.getCcdCaseId());
            return false;
        }

        // aac handles org policy modification based on the Change Organisation Request,
        // so we need to revert the org policies to their value before the event started
        // Refer to NoticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess
        boolean isReverted = false;
        if (isApplicantForRepresentationChange(finremCaseData)) {
            finremCaseData.setApplicantOrganisationPolicy(originalFinremCaseData.getApplicantOrganisationPolicy());
            isReverted = true;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            finremCaseData.setRespondentOrganisationPolicy(originalFinremCaseData.getRespondentOrganisationPolicy());
            isReverted = true;
        }

        // Going to apply decision
        if (isReverted) {
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(),
                buildCaseDetailsFromEventCaseData(info));
            return true;
        }
        throw new IllegalStateException(format("%s - ChangeOrganisationRequest populated with unknown or null NOC Party : %s",
            finremCaseData.getContactDetailsWrapper().getNocParty(),
            finremCaseData.getCcdCaseId()));
    }

    private CaseDetails buildCaseDetailsFromEventCaseData(StopRepresentingClientInfo info) {
        return finremCaseDetailsMapper.mapToCaseDetails(info.getCaseDetails());
    }

    private void clearChangeOrganisationRequestAfterThisEvent(CaseType caseType, long caseId) {
        // to reset the targeted field by case id and case type only
        // coreCaseDataService loads the case data again in the internal event call.
        coreCaseDataService.performPostSubmitCallback(caseType, caseId,
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(), caseDetails -> clearChangeOrganisationRequestField());
    }

    /**
     * Builds a {@link Representation} object indicating which parties
     * the current user represents in the given case.
     *
     * <p>This includes applicant, respondent, and intervener roles
     * (interveners 1–4), distinguishing between solicitor and barrister
     * representation.</p>
     *
     * @param caseData the case data containing the CCD case details
     * @param userAuthorisation the user's authorisation token
     * @return a {@link Representation} describing the user's representation
     *         across all parties in the case
     */
    public Representation buildRepresentation(FinremCaseData caseData, String userAuthorisation) {
        boolean isIntervenerRepresentative = caseRoleService.isIntervenerRepresentative(caseData, userAuthorisation);

        return new Representation(
            idamService.getIdamUserId(userAuthorisation),
            caseRoleService.isApplicantRepresentative(caseData, userAuthorisation),
            caseRoleService.isRespondentRepresentative(caseData, userAuthorisation),
            isIntervenerRepresentative ? caseRoleService.getIntervenerIndex(caseData, userAuthorisation).orElseThrow() : null,
            isIntervenerRepresentative ? (caseRoleService.getIntervenerSolicitorIndex(caseData, userAuthorisation).isEmpty()
                ? IntervenerRole.SOLICITOR : IntervenerRole.BARRISTER)
                : null
        );
    }

    /**
     * Checks whether the representing intervener barrister belongs to the same organisation
     * as the corresponding intervener solicitor.
     *
     * <p>The method:
     * <ul>
     *   <li>Returns {@code false} if the user is not representing any intervener barrister</li>
     *   <li>Finds the intervener based on the index in {@link Representation}</li>
     *   <li>Locates the barrister matching the current user ID</li>
     *   <li>Compares the barrister organisation with the intervener solicitor organisation</li>
     * </ul>
     *
     * @param caseData the financial remedy case data containing interveners and barristers
     * @param representation the current user representation details
     * @return {@code true} if the intervener barrister and solicitor are from the same organisation;
     *         {@code false} otherwise
     */
    public boolean isIntervenerBarristerFromSameOrganisationAsSolicitor(FinremCaseData caseData, Representation representation) {
        if (!representation.isRepresentingAnyIntervenerBarristers()) {
            return false;
        }
        int index = representation.intervenerIndex();
        IntervenerWrapper intervener = caseData.getInterveners().get(index - 1);
        List<BarristerCollectionItem> intvBarristers = caseData.getBarristerCollectionWrapper()
            .getIntervenerBarristersByIndex(index);

        Barrister barrister = emptyIfNull(intvBarristers).stream().map(BarristerCollectionItem::getValue)
            .filter(b -> b.getUserId().equals(representation.userId()))
            .findFirst().orElseThrow();

        return isSameOrganisation(
            ofNullable(intervener.getIntervenerOrganisation())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(Organisation.builder().build()),
            barrister.getOrganisation()
        );
    }

    /**
     * Determines whether two organisations are the same by comparing their organisation IDs.
     *
     * <p>
     * The comparison is null-safe. If an organisation or its ID is {@code null},
     * a default value is used to prevent {@link NullPointerException}.
     *
     * @param org1 the first organisation
     * @param org2 the second organisation
     * @return {@code true} if both organisations have the same organisation ID;
     *         {@code false} otherwise
     */
    public boolean isSameOrganisation(Organisation org1, Organisation org2) {
        return nullSafeOrganisationId(org1, " ")
            .equals(nullSafeOrganisationId(org2, "  "));
    }

    private String nullSafeOrganisationId(Organisation organisation, String defaultOrdId) {
        return ofNullable(organisation).map(Organisation::getOrganisationID).orElse(defaultOrdId);
    }
}
