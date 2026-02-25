package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.RESP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation.isSameOrganisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_APPLICANT_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_FOUR_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_FOUR_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_ONE_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_ONE_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_THREE_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_THREE_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_TWO_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_INTERVENER_TWO_SOLICITOR_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.FORMER_RESPONDENT_SOLICITOR_ONLY;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientService {

    private static final Revocation NO_NOC_INVOLVED = new Revocation(false, false);

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final ManageBarristerService manageBarristerService;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final CoreCaseDataService coreCaseDataService;

    private final IntervenerService intervenerService;

    private final CaseRoleService caseRoleService;

    private final IdamService idamService;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    private static FinremCaseData getFinremCaseDataBefore(StopRepresentingClientInfo info) {
        return info.getCaseDetailsBefore().getData();
    }

    private static FinremCaseData getFinremCaseData(StopRepresentingClientInfo info) {
        return info.getCaseDetails().getData();
    }

    private static long getCaseId(StopRepresentingClientInfo info) {
        return Long.parseLong(getFinremCaseData(info).getCcdCaseId());
    }

    private static Map<String, Object> clearChangeOrganisationRequestField() {
        Map<String, Object> map = new HashMap<>();
        map.put(CHANGE_ORGANISATION_REQUEST, null);
        return map;
    }

    private static EmailTemplateNames getNotifyApplicantRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_APPLICANT;
    }

    private static EmailTemplateNames getNotifyRespondentRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_RESPONDENT;
    }

    private static EmailTemplateNames getNotifyIntervenerRepresentativeTemplateName(FinremCaseData finremCaseData) {
        return finremCaseData.isContestedApplication()
            ? EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER
            : EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER;
    }

    /**
     * Revoke the case assess and send notifications to affected parties.
     *
     * <p>This method:
     * <ul>
     *   <li>Handles requests for intervener representatives</li>
     *   <li>Handles requests for applicant or respondent representatives</li>
     *   <li>Handles requests for any barrister representation changes</li>
     *   <li>Sends notification to parties like applicant, respondent and the parties being revoked</li>
     * </ul>
     *
     * @param info the stop-representing context containing case details and user authorisation
     */
    public void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
        handleIntervenerRepresentativeRequest(info);
        handleApplicantOrRespondentRepresentativeRequest(info);
        sendAllBarristerChangeToCaseAssignmentService(info);
    }

    /**
     * Builds a {@link RepresentativeInContext} object indicating which parties
     * the current user represents in the given case.
     *
     * <p>This includes applicant, respondent, and intervener roles
     * (interveners 1–4), distinguishing between solicitor and barrister
     * representation.</p>
     *
     * @param caseData the case data containing the CCD case details
     * @param userAuthorisation the user's authorisation token
     * @return a {@link RepresentativeInContext} describing the user's representation
     *         across all parties in the case
     */
    public RepresentativeInContext buildRepresentation(FinremCaseData caseData, String userAuthorisation) {
        boolean isIntervenerRepresentative = caseRoleService.isIntervenerRepresentative(caseData, userAuthorisation);

        Integer intervenerIndex = null;
        IntervenerRole intervenerRole = null;

        if (isIntervenerRepresentative) {
            intervenerIndex = caseRoleService
                .getIntervenerIndex(caseData, userAuthorisation)
                .orElseThrow();

            intervenerRole = caseRoleService
                .getIntervenerSolicitorIndex(caseData, userAuthorisation)
                .isEmpty()
                ? IntervenerRole.BARRISTER
                : IntervenerRole.SOLICITOR;
        }

        return new RepresentativeInContext(
            idamService.getIdamUserId(userAuthorisation),
            caseRoleService.isApplicantRepresentative(caseData, userAuthorisation),
            caseRoleService.isRespondentRepresentative(caseData, userAuthorisation),
            intervenerIndex,
            intervenerRole
        );
    }

    /**
     * Checks whether the representing intervener barrister belongs to the same organisation
     * as the corresponding intervener solicitor.
     *
     * <p>The method:
     * <ul>
     *   <li>Returns {@code false} if the user is not representing any intervener barrister</li>
     *   <li>Finds the intervener based on the index in {@link RepresentativeInContext}</li>
     *   <li>Locates the barrister matching the current user ID</li>
     *   <li>Compares the barrister organisation with the intervener solicitor organisation</li>
     * </ul>
     *
     * @param caseData the financial remedy case data containing interveners and barristers
     * @param representativeInContext the current user representation details
     * @return {@code true} if the intervener barrister and solicitor are from the same organisation;
     *         {@code false} otherwise
     */
    public boolean isIntervenerBarristerFromSameOrganisationAsSolicitor(FinremCaseData caseData, RepresentativeInContext representativeInContext) {
        if (!representativeInContext.isIntervenerBarrister()) {
            return false;
        }
        int index = representativeInContext.intervenerIndex();
        IntervenerWrapper intervener = caseData.getIntervenerById(index);
        List<BarristerCollectionItem> intvBarristers = caseData.getBarristerCollectionWrapper()
            .getIntervenerBarristersByIndex(index);

        Barrister barrister = emptyIfNull(intvBarristers).stream().map(BarristerCollectionItem::getValue)
            .filter(b -> b.getUserId().equals(representativeInContext.userId()))
            .findFirst().orElseThrow();

        return isSameOrganisation(
            ofNullable(intervener.getIntervenerOrganisation())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(Organisation.builder().build()),
            barrister.getOrganisation()
        );
    }

    /**
     * Marks the applicant as unrepresented.
     *
     * <p>
     * This sets the applicant represented flag to {@link YesOrNo#NO} and
     * clears the organisation policy currently assigned to the applicant solicitor role.
     *
     * @param finremCaseData the case data to update
     */
    public void setApplicantUnrepresented(FinremCaseData finremCaseData) {
        finremCaseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.NO);
        finremCaseData.setApplicantOrganisationPolicy(getDefaultOrganisationPolicy(APP_SOLICITOR));
    }

    /**
     * Marks the respondent as unrepresented.
     *
     * <p>
     * For consented applications, the consented respondent represented flag is updated.
     * For contested applications, the contested respondent represented flag is updated.
     * In both cases, it clears the organisation policy currently assigned to the respondent solicitor role.
     *
     * @param finremCaseData the case data to update
     */
    public void setRespondentUnrepresented(FinremCaseData finremCaseData) {
        if (finremCaseData.isConsentedApplication()) {
            finremCaseData.getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.NO);
        } else {
            finremCaseData.getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.NO);
        }
        finremCaseData.setRespondentOrganisationPolicy(getDefaultOrganisationPolicy(CaseRole.RESP_SOLICITOR));
    }

    /**
     * Marks an intervener as unrepresented.
     *
     * <p>
     * This sets the intervener represented flag to {@link YesOrNo#NO} and
     * clears the organisation policy currently assigned to the intervener solicitor role.
     *
     * @param intervenerWrapper the intervener wrapper to update
     */
    public void setIntervenerUnrepresented(IntervenerWrapper intervenerWrapper) {
        intervenerWrapper.setIntervenerRepresented(YesOrNo.NO);
        intervenerWrapper.setIntervenerOrganisation(getDefaultOrganisationPolicy(
            intervenerWrapper.getIntervenerSolicitorCaseRole()
        ));
    }

    private void handleApplicantOrRespondentRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseData(info);
        final CaseType caseType = finremCaseData.getCcdCaseType();
        final long caseId = getCaseId(info);

        Revocation revocation = revokeApplicantSolicitorOrRespondentSolicitor(info);

        if (revocation.isRevoked()) {
            // save a call if changeOrganisationRequestField is null
            clearChangeOrganisationRequestAfterThisEvent(caseType, caseId);

            if (revocation.applicantSolicitorRevoked) {
                notifyApplicantSolicitor(info);
            }
            if (revocation.respondentSolicitorRevoked) {
                notifyRespondentSolicitor(info);
            }
        }
    }

    /**
     * Determines whether intervener access should be revoked by comparing
     * the current intervener organisation with the original organisation
     * stored in case data.
     */
    private boolean shouldRevokeIntervenerAccess(IntervenerWrapper intervenerWrapper,
                                                 IntervenerWrapper originalIntervenerWrapper) {
        return !isSameOrganisation(
            ofNullable(intervenerWrapper.getIntervenerOrganisation())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(Organisation.builder().organisationID("SAME").build()),
            ofNullable(originalIntervenerWrapper.getIntervenerOrganisation())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(Organisation.builder().organisationID("SAME").build())
        );
    }

    private void handleIntervenerRepresentativeRequest(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseData(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBefore(info);

        // compare all interveners
        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (shouldRevokeIntervenerAccess(wrapper, originalWrapper)) {
                            intervenerService.revokeIntervenerSolicitor(info.getCaseDetails().getId(), originalWrapper);
                            notifyIntervenerSolicitor(info, it);
                        }
                    });
            }
        });
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
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBefore(info);

        BarristerChange barristerChange = manageBarristerService
            .getBarristerChange(info.getCaseDetails(), finremCaseDataBefore, barristerParty);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, barristerChange);
        SetUtils.emptyIfNull(barristerChange.getRemoved()).forEach(b -> {
            if (BarristerParty.APPLICANT.equals(barristerParty)) {
                notifyApplicantBarrister(info, b);
            }
            if (BarristerParty.RESPONDENT.equals(barristerParty)) {
                notifyRespondentBarrister(info, b);
            }
            IntStream.range(1, 5).forEach(i -> {
                if (BarristerParty.getIntervenerBarristerByIndex(i).equals(barristerParty)) {
                    notifyIntervenerBarrister(info, i, b);
                }
            });
        });
    }

    private record Revocation(boolean applicantSolicitorRevoked, boolean respondentSolicitorRevoked) {

        boolean isRevoked() {
            return applicantSolicitorRevoked || respondentSolicitorRevoked;
        }
    }

    private Revocation revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseData(info);
        final FinremCaseData originalFinremCaseData = getFinremCaseDataBefore(info);

        // to check if ChangeOrganisationRequest populated, otherwise skip it
        if (finremCaseData.getChangeOrganisationRequestField() == null) {
            log.info("{} - Not sending request to case assignment service due to changeOrganisationRequestField is null",
                finremCaseData.getCcdCaseId());
            return NO_NOC_INVOLVED;
        }

        // aac handles org policy modification based on the Change Organisation Request,
        // so we need to revert the org policies to their value before the event started
        // Refer to NoticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess
        boolean shouldPerformNoc = false;
        boolean isApplicantForRepresentationChange = isApplicantForRepresentationChange(finremCaseData);
        if (isApplicantForRepresentationChange) {
            finremCaseData.setApplicantOrganisationPolicy(originalFinremCaseData.getApplicantOrganisationPolicy());
            shouldPerformNoc = true;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            finremCaseData.setRespondentOrganisationPolicy(originalFinremCaseData.getRespondentOrganisationPolicy());
            shouldPerformNoc = true;
        }

        // Going to apply decision
        if (shouldPerformNoc) {
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(),
                buildCaseDetailsFromEventCaseData(info));
            return new Revocation(isApplicantForRepresentationChange, !isApplicantForRepresentationChange);
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

    private OrganisationPolicy getDefaultOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }

    private void sendRepresentativeNotification(
        StopRepresentingClientInfo info, List<NotificationParty> parties, EmailTemplateNames emailTemplate,
        NotificationRequest notificationRequest) {
        sendRepresentativeNotification(info, parties, emailTemplate, notificationRequest, null);
    }

    private void sendRepresentativeNotification(
        StopRepresentingClientInfo info, List<NotificationParty> parties, EmailTemplateNames emailTemplate,
        NotificationRequest notificationRequest, IntervenerType intervenerType
    ) {
        sendRepresentativeNotification(info, parties, emailTemplate, notificationRequest, intervenerType, null);
    }

    private void sendRepresentativeNotification(
        StopRepresentingClientInfo info, List<NotificationParty> parties, EmailTemplateNames emailTemplate,
        NotificationRequest notificationRequest, IntervenerType intervenerType, Barrister barrister
    ) {
        String userAuthorisation = info.getUserAuthorisation();

        applicationEventPublisher.publishEvent(SendCorrespondenceEvent.builder()
            .notificationParties(parties)
            .emailNotificationRequest(notificationRequest)
            .emailTemplate(emailTemplate)
            .caseDetails(info.getCaseDetails())
            .caseDetailsBefore(info.getCaseDetailsBefore())
            .authToken(userAuthorisation)
            .intervenerType(intervenerType)
            .barrister(barrister)
            .build()
        );
    }

    private void notifyApplicantBarrister(StopRepresentingClientInfo info, Barrister barrister) {
        sendRepresentativeNotification(
            info,
            List.of(FORMER_APPLICANT_BARRISTER_ONLY),
            getNotifyApplicantRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            null, barrister
        );
    }

    private void notifyApplicantSolicitor(StopRepresentingClientInfo info) {
        sendRepresentativeNotification(
            info,
            List.of(FORMER_APPLICANT_SOLICITOR_ONLY),
            getNotifyApplicantRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR)
        );
    }

    private void notifyRespondentBarrister(StopRepresentingClientInfo info, Barrister barrister) {
        sendRepresentativeNotification(
            info,
            List.of(FORMER_RESPONDENT_BARRISTER_ONLY),
            getNotifyRespondentRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            null, barrister
        );
    }

    private void notifyRespondentSolicitor(StopRepresentingClientInfo info) {
        sendRepresentativeNotification(
            info,
            List.of(FORMER_RESPONDENT_SOLICITOR_ONLY),
            getNotifyRespondentRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), RESP_SOLICITOR)
        );
    }

    private void notifyIntervenerBarrister(StopRepresentingClientInfo info, int intervenerId, Barrister barrister) {
        IntervenerType intervenerType = Arrays.stream(IntervenerType.values())
            .filter(d -> d.getIntervenerId() == intervenerId)
            .findFirst()
            .orElse(null);

        sendRepresentativeNotification(
            info,
            List.of(resolveIntervenerBarristerNotificationParty(intervenerId)),
            getNotifyIntervenerRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister, intervenerType),
            intervenerType, barrister
        );
    }

    private void notifyIntervenerSolicitor(StopRepresentingClientInfo info, IntervenerType intervenerType) {
        int intervenerId = intervenerType.getIntervenerId();
        sendRepresentativeNotification(
            info,
            List.of(resolveIntervenerSolicitorNotificationParty(intervenerId)),
            getNotifyIntervenerRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(),
                    CaseRole.getIntervenerSolicitorByIndex(intervenerId), intervenerType),
            intervenerType
        );
    }

    private NotificationParty resolveIntervenerBarristerNotificationParty(int index) {
        return switch(index) {
            case 1 -> FORMER_INTERVENER_ONE_BARRISTER_ONLY;
            case 2 -> FORMER_INTERVENER_TWO_BARRISTER_ONLY;
            case 3 -> FORMER_INTERVENER_THREE_BARRISTER_ONLY;
            case 4 -> FORMER_INTERVENER_FOUR_BARRISTER_ONLY;
            default -> throw new IllegalArgumentException("Invalid index " + index);
        };
    }

    private NotificationParty resolveIntervenerSolicitorNotificationParty(int index) {
        return switch(index) {
            case 1 -> FORMER_INTERVENER_ONE_SOLICITOR_ONLY;
            case 2 -> FORMER_INTERVENER_TWO_SOLICITOR_ONLY;
            case 3 -> FORMER_INTERVENER_THREE_SOLICITOR_ONLY;
            case 4 -> FORMER_INTERVENER_FOUR_SOLICITOR_ONLY;
            default -> throw new IllegalArgumentException("Invalid index " + index);
        };
    }
}
