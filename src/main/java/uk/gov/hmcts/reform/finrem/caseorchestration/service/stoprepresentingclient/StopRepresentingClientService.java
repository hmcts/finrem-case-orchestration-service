package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventEnvelop;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
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

    private static final LitigantRevocation NO_NOC_INVOLVED = new LitigantRevocation(false, false);

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

    private final GenericDocumentService genericDocumentService;

    private final DocumentConfiguration documentConfiguration;

    private final LetterDetailsMapper letterDetailsMapper;

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
    @Deprecated
    public void revokePartiesAccessAndNotifyParties(StopRepresentingClientInfo info) {
//        handleIntervenerRepresentativeRequest(info);
//        handleApplicantOrRespondentRepresentativeRequest(info);
        revokeAllPartiesBarrister(info);
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
     * Marks the applicant as unrepresented and removes any existing solicitor details.
     *
     * <p>This method:
     * <ul>
     *   <li>Sets the applicant represented flag to {@link YesOrNo#NO}</li>
     *   <li>Clears all applicant solicitor contact and firm details</li>
     *   <li>Resets the applicant solicitor organisation policy to the default value</li>
     * </ul>
     *
     * <p>This ensures that any previously assigned solicitor information is removed
     * when the applicant is no longer represented.
     *
     * @param finremCaseData the case data to update
     */
    public void setApplicantUnrepresented(FinremCaseData finremCaseData) {
        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();
        contactDetailsWrapper.setApplicantRepresented(YesOrNo.NO);

        // consented & contested
        contactDetailsWrapper.setSolicitorReference(null);
        // consented
        contactDetailsWrapper.setSolicitorName(null);
        contactDetailsWrapper.setSolicitorFirm(null);
        contactDetailsWrapper.setSolicitorAddress(null);
        contactDetailsWrapper.setSolicitorPhone(null);
        contactDetailsWrapper.setSolicitorEmail(null);
        contactDetailsWrapper.setSolicitorDxNumber(null);
        contactDetailsWrapper.setSolicitorAgreeToReceiveEmails(null);
        // contested
        contactDetailsWrapper.setApplicantSolicitorName(null);
        contactDetailsWrapper.setApplicantSolicitorFirm(null);
        contactDetailsWrapper.setApplicantSolicitorAddress(null);
        contactDetailsWrapper.setApplicantSolicitorPhone(null);
        contactDetailsWrapper.setApplicantSolicitorEmail(null);
        contactDetailsWrapper.setApplicantSolicitorDxNumber(null);
        contactDetailsWrapper.setApplicantSolicitorConsentForEmails(null);

        finremCaseData.setApplicantOrganisationPolicy(getDefaultOrganisationPolicy(APP_SOLICITOR));
    }

    /**
     * Marks the respondent as unrepresented.
     *
     * <p>This method clears the respondent solicitor details and updates the
     * respondent represented flag depending on the application type:
     * <ul>
     *   <li>For consented applications, {@code consentedRespondentRepresented} is set to {@link YesOrNo#NO}.</li>
     *   <li>For contested applications, {@code contestedRespondentRepresented} is set to {@link YesOrNo#NO}.</li>
     * </ul>
     * It also resets the organisation policy assigned to the respondent solicitor role.
     *
     * @param finremCaseData the case data to update
     */
    public void setRespondentUnrepresented(FinremCaseData finremCaseData) {
        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();

        // consented & contested
        contactDetailsWrapper.setRespondentSolicitorName(null);
        contactDetailsWrapper.setRespondentSolicitorFirm(null);
        contactDetailsWrapper.setRespondentSolicitorReference(null);
        contactDetailsWrapper.setRespondentSolicitorAddress(null);
        contactDetailsWrapper.setRespondentSolicitorPhone(null);
        contactDetailsWrapper.setRespondentSolicitorEmail(null);
        contactDetailsWrapper.setRespondentSolicitorDxNumber(null);

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

        intervenerWrapper.setIntervenerSolEmail(null);
        intervenerWrapper.setIntervenerSolicitorFirm(null);
        intervenerWrapper.setIntervenerSolicitorReference(null);
        intervenerWrapper.setIntervenerSolName(null);
        intervenerWrapper.setIntervenerSolPhone(null);
    }

    /**
     * Prepares a list of {@link SendCorrespondenceEventEnvelop} for notifying litigants
     * (applicant or respondent) whose representation has been revoked.
     *
     * <p>This method does not send the notifications directly. It constructs the
     * correspondence event envelopes (email and letter) which will be processed later
     * in the workflow to trigger the actual notifications.</p>
     *
     * @param litigantRevocation flags indicating which litigants' representation was revoked
     * @param info the stop representing client event information
     * @return a list of {@link SendCorrespondenceEventEnvelop} for later notification processing
     */
    public List<SendCorrespondenceEventEnvelop> prepareLitigantRevocationNotificationEvents(LitigantRevocation litigantRevocation,
                                                                                            StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> notificationEnvelops = new ArrayList<>();
        if (litigantRevocation.wasRevoked()) {

            if (litigantRevocation.applicantSolicitorRevoked) {
                notificationEnvelops.add(prepareApplicantSolicitorEmailNotificationEvent(info));
                notificationEnvelops.add(prepareApplicantLetterNotificationEvent(info));
            }
            if (litigantRevocation.respondentSolicitorRevoked) {
                notificationEnvelops.add(prepareRespondentSolicitorEmailNotificationEvent(info));
                notificationEnvelops.add(prepareRespondentLetterNotificationEvent(info));
            }
        }
        return notificationEnvelops;
    }

    /**
     * Revokes the solicitor for a given intervener and prepares the corresponding
     * email notification event.
     *
     * <p>This method calls {@link IntervenerService#revokeIntervenerSolicitor(long, IntervenerWrapper)}
     * to revoke the solicitor’s access. It then constructs a {@link SendCorrespondenceEventEnvelop}
     * to notify the intervener's solicitor that representation has stopped. The email
     * is not sent directly here; the returned envelope will be processed later in the
     * correspondence workflow.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerWrapper wrapper containing the intervener details
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    public SendCorrespondenceEventEnvelop revokeIntervenerSolicitor(StopRepresentingClientInfo info,
                                                                    IntervenerWrapper intervenerWrapper) {
        intervenerService.revokeIntervenerSolicitor(getCaseId(info), intervenerWrapper);
        return prepareIntervenerSolicitorEmailNotificationEvent(info, intervenerWrapper.getIntervenerType());
    }

    /**
     * Identifies the intervener solicitors whose access should be revoked based on
     * changes in the case data.
     *
     * <p>This method compares the current {@link FinremCaseData} with the previous
     * case data to detect any interveners whose solicitor access needs to be revoked.
     * For each intervener present in both snapshots, it evaluates whether revocation
     * is required using {@link #shouldRevokeIntervenerSolicitorAccess(IntervenerWrapper, IntervenerWrapper)}.</p>
     *
     * <p>No changes are made to the case data or notifications sent; this method
     * only returns the list of interveners for whom revocation should be applied.</p>
     *
     * @param info the stop representing client event information
     * @return a list of {@link IntervenerWrapper} objects representing interveners
     *         whose solicitor access should be revoked
     */
    public List<IntervenerWrapper> getToBeRevokedIntervenerSolicitors(StopRepresentingClientInfo info) {
        final FinremCaseData finremCaseData = getFinremCaseData(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBefore(info);

        List<IntervenerWrapper> ret = new ArrayList<>();

        // compare all interveners
        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (shouldRevokeIntervenerSolicitorAccess(wrapper, originalWrapper)) {
                            ret.add(originalWrapper);
                        }
                    });
            }
        });
        return ret;
    }

    /**
     * Determines whether intervener access should be revoked by comparing
     * the current intervener organisation with the original organisation
     * stored in case data.
     */
    private boolean shouldRevokeIntervenerSolicitorAccess(IntervenerWrapper intervenerWrapper,
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

    // TODO
    public List<SendCorrespondenceEventEnvelop> revokeAllPartiesBarrister(StopRepresentingClientInfo info) {
        List<SendCorrespondenceEventEnvelop> ret = new ArrayList<>();
        ret.addAll(revokeGivenBarrister(info, BarristerParty.APPLICANT));
        ret.addAll(revokeGivenBarrister(info, BarristerParty.RESPONDENT));
        ret.addAll(revokeGivenBarrister(info, BarristerParty.INTERVENER1));
        ret.addAll(revokeGivenBarrister(info, BarristerParty.INTERVENER2));
        ret.addAll(revokeGivenBarrister(info, BarristerParty.INTERVENER3));
        ret.addAll(revokeGivenBarrister(info, BarristerParty.INTERVENER4));
        return ret;
    }

    public record LitigantRevocation(boolean applicantSolicitorRevoked, boolean respondentSolicitorRevoked) {

        public boolean wasRevoked() {
            return applicantSolicitorRevoked || respondentSolicitorRevoked;
        }
    }

    /**
     * Revokes the applicant or respondent solicitor when a "Stop Representing Client" event is triggered.
     *
     * <p>This method determines whether a Notice of Change (NoC) operation should be performed based on
     * the {@link ChangeOrganisationRequest} present in the case data.</p>
     *
     * <p>If no organisation change is requested (i.e. the change organisation request is null or
     * contains no organisations to add or remove), no action is taken and {@link #NO_NOC_INVOLVED}
     * is returned.</p>
     *
     * <p>When a representation change is detected, the organisation policy for the affected party
     * (applicant or respondent) is restored to its original value before the event started. This is
     * required because the Access and Assignment Controller (AAC) modifies organisation policies
     * based on the change organisation request.</p>
     *
     * <p>After restoring the original organisation policy, the decision is applied via the
     * Case Assignment service.</p>
     *
     * @param info the event information containing the current and previous case data
     * @return a {@link LitigantRevocation} object indicating which solicitor (applicant or respondent)
     *         has been revoked, or {@link #NO_NOC_INVOLVED} if no organisation change was required
     * @throws IllegalStateException if the change organisation request is populated but the
     *         Notice of Change party cannot be determined
     */
    public LitigantRevocation revokeApplicantSolicitorOrRespondentSolicitor(StopRepresentingClientInfo info) {
        CaseDetails clonedCaseDetails = cloneCaseDetailsFromFinremCaseDetails(info);

        final FinremCaseData finremCaseData = getFinremCaseData(info);
        final FinremCaseData originalFinremCaseData = getFinremCaseDataBefore(info);

        // to check if ChangeOrganisationRequest populated, otherwise skip it
        if (Optional.ofNullable(finremCaseData.getChangeOrganisationRequestField())
            .map(ChangeOrganisationRequest::isNoOrganisationsToAddOrRemove)
                .orElse(true)) {
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
            //finremCaseData.setApplicantOrganisationPolicy(originalFinremCaseData.getApplicantOrganisationPolicy());
            clonedCaseDetails.getData().put("ApplicantOrganisationPolicy", originalFinremCaseData.getApplicantOrganisationPolicy());
            shouldPerformNoc = true;
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            //finremCaseData.setRespondentOrganisationPolicy(originalFinremCaseData.getRespondentOrganisationPolicy());
            clonedCaseDetails.getData().put("RespondentOrganisationPolicy", originalFinremCaseData.getRespondentOrganisationPolicy());
            shouldPerformNoc = true;
        }

        // Going to apply decision
        if (shouldPerformNoc) {
            assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), clonedCaseDetails);
            return new LitigantRevocation(isApplicantForRepresentationChange, !isApplicantForRepresentationChange);
        }
        throw new IllegalStateException(format("%s - ChangeOrganisationRequest populated with unknown or null NOC Party : %s",
            finremCaseData.getContactDetailsWrapper().getNocParty(),
            finremCaseData.getCcdCaseId()));
    }

    private List<SendCorrespondenceEventEnvelop> revokeGivenBarrister(StopRepresentingClientInfo info, BarristerParty barristerParty) {
        final long caseId = getCaseId(info);
        final FinremCaseData finremCaseDataBefore = getFinremCaseDataBefore(info);
        List<SendCorrespondenceEventEnvelop> notificationEnvelops = new ArrayList<>();

        BarristerChange barristerChange = manageBarristerService
            .getBarristerChange(info.getCaseDetails(), finremCaseDataBefore, barristerParty);
        barristerChangeCaseAccessUpdater.executeBarristerChange(caseId, barristerChange);
        SetUtils.emptyIfNull(barristerChange.getRemoved()).forEach(b -> {
            if (BarristerParty.APPLICANT.equals(barristerParty)) {
                notificationEnvelops.add(prepareApplicantBarristerEmailNotificationEvent(info, b));
            }
            if (BarristerParty.RESPONDENT.equals(barristerParty)) {
                notificationEnvelops.add(prepareRespondentBarristerEmailNotificationEvent(info, b));
            }
            IntStream.range(1, 5).forEach(i -> {
                if (BarristerParty.getIntervenerBarristerByIndex(i).equals(barristerParty)) {
                    notificationEnvelops.add(prepareIntervenerBarristerEmailNotificationEvent(info, i, b));
                }
            });
        });
        return notificationEnvelops;
    }

    private CaseDetails cloneCaseDetailsFromFinremCaseDetails(StopRepresentingClientInfo info) {
        return finremCaseDetailsMapper.mapToCaseDetails(info.getCaseDetails());
    }

    /**
     * Performs cleanup of the 'Change Organisation Request' field after the
     * Notice of Change (NOC) workflow has completed.
     *
     * <p>This method triggers a post-submit callback on the {@link CoreCaseDataService}
     * to reset the targeted field using only the case type and case ID. The callback
     * reloads the case data internally and invokes
     * {@link #clearChangeOrganisationRequestField()} to clear the field.</p>
     *
     * <p>No notifications or other side effects are performed; this is purely a data
     * cleanup operation.</p>
     *
     * @param info the stop representing client event information, containing the case details
     */
    public void performCleanUpAfterNocWorkflow(StopRepresentingClientInfo info) {
        final CaseType caseType = info.getCaseDetails().getCaseType();

        // to reset the targeted field by case id and case type only
        // coreCaseDataService loads the case data again in the internal event call.
        coreCaseDataService.performPostSubmitCallback(caseType, getCaseId(info),
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(), caseDetails -> clearChangeOrganisationRequestField());
    }

    private OrganisationPolicy getDefaultOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }

    private SendCorrespondenceEventEnvelop prepareRepresentativeEmailNotificationEvent(String description,
                                                                                       StopRepresentingClientInfo info, List<NotificationParty> parties, EmailTemplateNames emailTemplate,
                                                                                       NotificationRequest notificationRequest) {
        return prepareRepresentativeEmailNotificationEvent(description, info, parties, emailTemplate, notificationRequest, null);
    }

    private SendCorrespondenceEventEnvelop prepareRepresentativeEmailNotificationEvent(String description,
                                                                                       StopRepresentingClientInfo info, List<NotificationParty> parties, EmailTemplateNames emailTemplate,
                                                                                       NotificationRequest notificationRequest, Barrister barrister) {
        String userAuthorisation = info.getUserAuthorisation();

        return SendCorrespondenceEventEnvelop.builder()
            .description(description)
            .event(SendCorrespondenceEvent.builder()
                .notificationParties(parties)
                .emailNotificationRequest(notificationRequest)
                .emailTemplate(emailTemplate)
                .caseDetails(info.getCaseDetails())
                .caseDetailsBefore(info.getCaseDetailsBefore())
                .authToken(userAuthorisation)
                .barrister(barrister)
                .build()
            )
            .build();
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to the applicant's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the notification details and template
     * required to notify the former applicant solicitor. The envelope will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareApplicantSolicitorEmailNotificationEvent(StopRepresentingClientInfo info) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying applicant solicitor",
            info,
            List.of(FORMER_APPLICANT_SOLICITOR_ONLY),
            getNotifyApplicantRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), APP_SOLICITOR)
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to the applicant's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the notification details and template
     * required to notify the former applicant barrister. The envelope will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param barrister the applicant barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareApplicantBarristerEmailNotificationEvent(StopRepresentingClientInfo info, Barrister barrister) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying applicant barrister",
            info,
            List.of(FORMER_APPLICANT_BARRISTER_ONLY),
            getNotifyApplicantRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            barrister
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to an intervener's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the required notification details.
     * The envelope will be processed later in the correspondence workflow to trigger
     * the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerType the intervener whose solicitor should receive the notification
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareIntervenerSolicitorEmailNotificationEvent(StopRepresentingClientInfo info, IntervenerType intervenerType) {
        int intervenerId = intervenerType.getIntervenerId();
        return prepareRepresentativeEmailNotificationEvent(
            "notifying intervener %s solicitor".formatted(intervenerId),
            info,
            List.of(resolveIntervenerSolicitorNotificationParty(intervenerId)),
            getNotifyIntervenerRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(),
                    CaseRole.getIntervenerSolicitorByIndex(intervenerId), intervenerType)
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to an intervener's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the notification details and template
     * required to notify the former intervener barrister. The envelope will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerId the identifier of the intervener whose barrister should receive the notification
     * @param barrister the intervener barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareIntervenerBarristerEmailNotificationEvent(StopRepresentingClientInfo info, int intervenerId, Barrister barrister) {
        IntervenerType intervenerType = Arrays.stream(IntervenerType.values())
            .filter(d -> d.getIntervenerId() == intervenerId)
            .findFirst()
            .orElse(null);

        return prepareRepresentativeEmailNotificationEvent(
            "notifying intervener barrister",
            info,
            List.of(resolveIntervenerBarristerNotificationParty(intervenerId)),
            getNotifyIntervenerRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister, intervenerType),
            barrister
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for generating a letter notification
     * to a specific party when representation has stopped.
     *
     * <p>This method does not send the notification directly. Instead, it constructs the
     * correspondence event envelope containing the party, case details, and generated
     * document to be posted. The envelope will be processed later in the correspondence
     * workflow to trigger the actual letter notification.</p>
     *
     * @param description a description of the correspondence event
     * @param info the stop representing client event information containing case details and authorisation
     * @param notificationParty the party who should receive the notification
     * @param documentGenerator function used to generate the document to be posted
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later processing
     */
    private SendCorrespondenceEventEnvelop preparePartyLetterNotificationEvent(String description, StopRepresentingClientInfo info,
                                                                               NotificationParty notificationParty,
                                                                               Function<StopRepresentingClientInfo, CaseDocument> documentGenerator) {
        return SendCorrespondenceEventEnvelop.builder()
            .description(description)
            .event(SendCorrespondenceEvent.builder()
                .letterNotificationOnly(true)
                .notificationParties(List.of(notificationParty))
                .caseDetails(info.getCaseDetails())
                .caseDetailsBefore(info.getCaseDetailsBefore())
                .authToken(info.getUserAuthorisation())
                .documentsToPost(List.of(documentGenerator.apply(info)))
                .build()
            ).build();
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending a stop representing
     * letter notification to the applicant.
     *
     * <p>The generated envelope includes the applicant as the notification party and
     * attaches the stop representing applicant letter. The correspondence event will
     * be processed later in the workflow to generate and send the letter.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventEnvelop} for applicant notification
     */
    private SendCorrespondenceEventEnvelop prepareApplicantLetterNotificationEvent(StopRepresentingClientInfo info) {
        return preparePartyLetterNotificationEvent(
            "notifying applicant",
            info,
            NotificationParty.APPLICANT,
            i -> generateStopRepresentingApplicantLetter(i.getCaseDetails(), i.getUserAuthorisation())
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending a stop representing
     * letter notification to the respondent.
     *
     * <p>The generated envelope includes the respondent as the notification party and
     * attaches the stop representing respondent letter. The correspondence event will
     * be processed later in the workflow to generate and send the letter.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventEnvelop} for respondent notification
     */
    private SendCorrespondenceEventEnvelop prepareRespondentLetterNotificationEvent(StopRepresentingClientInfo info) {
        return preparePartyLetterNotificationEvent(
            "notifying respondent",
            info,
            NotificationParty.RESPONDENT,
            i -> generateStopRepresentingRespondentLetter(i.getCaseDetails(), i.getUserAuthorisation())
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to the respondent's solicitor when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the notification details and template
     * required to notify the former respondent solicitor. The envelope will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareRespondentSolicitorEmailNotificationEvent(StopRepresentingClientInfo info) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying respondent solicitor",
            info,
            List.of(FORMER_RESPONDENT_SOLICITOR_ONLY),
            getNotifyRespondentRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), RESP_SOLICITOR)
        );
    }

    /**
     * Prepares a {@link SendCorrespondenceEventEnvelop} for sending an email notification
     * to the respondent's barrister when representation has stopped.
     *
     * <p>This method does not send the email directly. Instead, it constructs a
     * correspondence event envelope containing the notification details and template
     * required to notify the former respondent barrister. The envelope will be processed
     * later in the correspondence workflow to trigger the actual email notification.</p>
     *
     * @param info the stop representing client event information
     * @param barrister the respondent barrister who should receive the notification
     * @return a populated {@link SendCorrespondenceEventEnvelop} for later email notification processing
     */
    private SendCorrespondenceEventEnvelop prepareRespondentBarristerEmailNotificationEvent(StopRepresentingClientInfo info,
                                                                                            Barrister barrister) {
        return prepareRepresentativeEmailNotificationEvent(
            "notifying respondent barrister",
            info,
            List.of(FORMER_RESPONDENT_BARRISTER_ONLY),
            getNotifyRespondentRepresentativeTemplateName(getFinremCaseData(info)),
            finremNotificationRequestMapper
                .getNotificationRequestForStopRepresentingClientEmail(info.getCaseDetailsBefore(), barrister),
            barrister
        );
    }

    private CaseDocument generateStopRepresentingLetter(FinremCaseDetails finremCaseDetails,
                                                        String authorisationToken,
                                                        DocumentHelper.PaperNotificationRecipient recipient,
                                                        String filenamePrefix,
                                                        String template) {
        Map<String, Object> documentDataMap =
            letterDetailsMapper.getLetterDetailsAsMap(finremCaseDetails, recipient);

        String documentFilename = format("%s_%s.pdf",
            filenamePrefix,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        return genericDocumentService.generateDocumentFromPlaceholdersMap(
            authorisationToken,
            documentDataMap,
            template,
            documentFilename,
            finremCaseDetails.getCaseType()
        );
    }

    private CaseDocument generateStopRepresentingApplicantLetter(FinremCaseDetails finremCaseDetails,
                                                                 String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            APPLICANT,
            "ApplicantRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToApplicantTemplate()
        );
    }

    private CaseDocument generateStopRepresentingRespondentLetter(FinremCaseDetails finremCaseDetails,
                                                                  String authorisationToken) {
        return generateStopRepresentingLetter(
            finremCaseDetails,
            authorisationToken,
            RESPONDENT,
            "RespondentRepresentationRemovalNotice",
            documentConfiguration.getStopRepresentingLetterToRespondentTemplate()
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
