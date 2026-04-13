package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEventWithDescription;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.INTERNAL_CHANGE_UPDATE_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.APP_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isApplicantForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty.isRespondentForRepresentationChange;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation.isSameOrganisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy.getDefaultOrganisationPolicy;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocUtils.clearChangeOrganisationRequestField;

@Service
@Slf4j
@RequiredArgsConstructor
public class StopRepresentingClientService {

    private static final LitigantRevocation NO_NOC_INVOLVED = new LitigantRevocation(false, false);

    private final AssignCaseAccessService assignCaseAccessService;

    private final SystemUserService systemUserService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    private final CoreCaseDataService coreCaseDataService;

    private final IntervenerService intervenerService;

    private final CaseRoleService caseRoleService;

    private final IdamService idamService;

    private final StopRepresentingClientCorresponder stopRepresentingClientCorresponder;

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

        IntervenerType intervenerType = null;
        IntervenerRole intervenerRole = null;

        if (isIntervenerRepresentative) {
            intervenerType = caseRoleService
                .getIntervenerType(caseData, userAuthorisation)
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
            intervenerType,
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
        IntervenerWrapper intervener = caseData.getIntervenerById(representativeInContext.intervenerType().getIntervenerId());
        List<BarristerCollectionItem> intvBarristers = caseData.getBarristerCollectionWrapper()
            .getIntervenerBarristers(representativeInContext.intervenerType());

        Barrister barrister = emptyIfNull(intvBarristers).stream().map(BarristerCollectionItem::getValue)
            .filter(b -> b.getUserId().equals(representativeInContext.userId()))
            .findFirst().orElseThrow();

        OrganisationPolicy policy = intervener.getIntervenerOrganisation();
        return isSameOrganisation(
            policy == null ? null : policy.getOrganisation(),
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
        contactDetailsWrapper.clearApplicantSolicitorFields();

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
        contactDetailsWrapper.clearRespondentSolicitorFields();

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
        intervenerWrapper.clearIntervenerSolicitorFields();
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
        final FinremCaseData finremCaseData = info.getFinremCaseData();
        final FinremCaseData finremCaseDataBefore = info.getFinremCaseDataBefore();

        List<IntervenerWrapper> intervenerWrappers = new ArrayList<>();

        // compare all interveners
        finremCaseDataBefore.getInterveners().forEach(originalWrapper -> {
            IntervenerType it = originalWrapper.getIntervenerType();
            if (it != null) {
                finremCaseData.getInterveners().stream()
                    .filter(wrapper -> it.equals(wrapper.getIntervenerType()))
                    .findAny()
                    .ifPresent(wrapper -> {
                        if (shouldRevokeIntervenerSolicitorAccess(wrapper, originalWrapper)) {
                            intervenerWrappers.add(originalWrapper);
                        }
                    });
            }
        });
        return intervenerWrappers;
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
        FinremCaseData finremCaseData = info.getFinremCaseData();
        FinremCaseData originalFinremCaseData = info.getFinremCaseDataBefore();

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
        boolean isApplicantForRepresentationChange = isApplicantForRepresentationChange(finremCaseData);
        boolean isRespondentForRepresentationChange = isRespondentForRepresentationChange(finremCaseData);

        if (!isApplicantForRepresentationChange && !isRespondentForRepresentationChange) {
            throw new IllegalStateException(format("%s - ChangeOrganisationRequest populated with unknown or null NOC Party : %s",
                finremCaseData.getContactDetailsWrapper().getNocParty(),
                finremCaseData.getCcdCaseId()));
        }

        CaseDetails clonedCaseDetails = cloneCaseDetailsFromFinremCaseDetails(info);
        if (isApplicantForRepresentationChange) {
            clonedCaseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, originalFinremCaseData.getApplicantOrganisationPolicy());
        } else if (isRespondentForRepresentationChange(finremCaseData)) {
            clonedCaseDetails.getData().put(ORGANISATION_POLICY_RESPONDENT, originalFinremCaseData.getRespondentOrganisationPolicy());
        }

        // Going to apply decision
        log.info("{} - about to send a NOC request to case assignment API", info.getCaseId());
        assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), clonedCaseDetails);
        return new LitigantRevocation(isApplicantForRepresentationChange, !isApplicantForRepresentationChange);
    }

    /**
     * Revokes the solicitor for a given intervener and prepares the corresponding
     * email notification event.
     *
     * <p>This method calls {@link IntervenerService#revokeIntervenerSolicitor(long, IntervenerWrapper)}
     * to revoke the solicitor’s access. It then constructs a {@link SendCorrespondenceEventWithDescription}
     * to notify the intervener's solicitor that representation has stopped. The email
     * is not sent directly here; the returned event will be processed later in the
     * correspondence workflow.</p>
     *
     * @param info the stop representing client event information
     * @param intervenerWrapper wrapper containing the intervener details
     * @return a populated {@link SendCorrespondenceEventWithDescription} for later email notification processing
     */
    public SendCorrespondenceEventWithDescription revokeIntervenerSolicitor(StopRepresentingClientInfo info,
                                                                            IntervenerWrapper intervenerWrapper) {
        intervenerService.revokeIntervenerSolicitor(info.getCaseId(), intervenerWrapper);
        return stopRepresentingClientCorresponder
            .prepareIntervenerSolicitorEmailNotificationEvent(info, intervenerWrapper.getIntervenerType());
    }

    /**
     * Revokes the representation of barristers for a given case and generates
     * corresponding email notification events for the removed barristers.
     *
     * <p>
     * The method performs the following steps:
     * <ol>
     *     <li>Updates case access for the barristers being removed via {@code barristerChangeCaseAccessUpdater}.</li>
     *     <li>Iterates over the list of removed barristers and generates the appropriate email notification
     *         event depending on the {@link BarristerParty} type:
     *         <ul>
     *             <li>Applicant barristers</li>
     *             <li>Respondent barristers</li>
     *             <li>Intervener barristers (up to 4 interveners)</li>
     *         </ul>
     *     </li>
     *     <li>Filters out any null notifications and returns a list of {@link SendCorrespondenceEventWithDescription}.</li>
     * </ol>
     *
     * @param info           the information about the client whose barristers are being revoked
     * @param barristerChange the details of the barrister change, including which barristers are removed
     * @return a list of {@link SendCorrespondenceEventWithDescription} representing email notifications
     *         to be sent for the revoked barristers; empty list if no barristers were removed
     */
    public List<SendCorrespondenceEventWithDescription> revokeBarristers(StopRepresentingClientInfo info, BarristerChange barristerChange) {
        barristerChangeCaseAccessUpdater.executeBarristerChange(info.getCaseId(), barristerChange);

        BarristerParty barristerParty = barristerChange.getBarristerParty();
        return SetUtils.emptyIfNull(barristerChange.getRemoved())
            .stream()
            .map(b -> switch (barristerParty) {
                case APPLICANT -> stopRepresentingClientCorresponder
                    .prepareApplicantBarristerEmailNotificationEvent(info, b);
                case RESPONDENT -> stopRepresentingClientCorresponder
                    .prepareRespondentBarristerEmailNotificationEvent(info, b);
                case INTERVENER1 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_ONE, b);
                case INTERVENER2 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_TWO, b);
                case INTERVENER3 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_THREE, b);
                case INTERVENER4 -> stopRepresentingClientCorresponder
                    .prepareIntervenerBarristerEmailNotificationEvent(info, IntervenerType.INTERVENER_FOUR, b);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Performs post–Notice of Change (NOC) cleanup for the specified case.
     *
     * <p>This method resets the {@code Change Organisation Request} field after the
     * NOC workflow has completed. It invokes a post-submit callback through
     * {@link CoreCaseDataService}, using only the case type and case ID.</p>
     *
     * <p>The {@link CoreCaseDataService} internally reloads the latest case data
     * before executing the update event, ensuring the field is cleared using
     * {@link NocUtils#clearChangeOrganisationRequestField()}.</p>
     *
     * <p>This operation performs a data cleanup only and does not trigger
     * notifications or additional business processing.</p>
     *
     * @param info the stop representing client context containing the case details
     *             and identifiers required to perform the cleanup
     */
    public void performCleanUpAfterNocWorkflow(StopRepresentingClientInfo info) {
        final CaseType caseType = info.getCaseDetails().getCaseType();

        log.info("{} - about to perform clean-up job after NOC workflow", info.getCaseId());

        // to reset the targeted field by case id and case type only
        // coreCaseDataService loads the case data again in the internal event call.
        coreCaseDataService.performPostSubmitCallback(caseType, info.getCaseId(),
            INTERNAL_CHANGE_UPDATE_CASE.getCcdType(), caseDetails -> clearChangeOrganisationRequestField());
    }

    private CaseDetails cloneCaseDetailsFromFinremCaseDetails(StopRepresentingClientInfo info) {
        return finremCaseDetailsMapper.mapToCaseDetails(info.getCaseDetails());
    }

    /**
     * Determines whether intervener access should be revoked by comparing
     * the current intervener organisation with the original organisation
     * stored in case data.
     */
    private boolean shouldRevokeIntervenerSolicitorAccess(
        IntervenerWrapper newWrapper,
        IntervenerWrapper oldWrapper) {

        return !Objects.equals(
            getOrganisationId(newWrapper),
            getOrganisationId(oldWrapper)
        );
    }

    private String getOrganisationId(IntervenerWrapper wrapper) {
        OrganisationPolicy policy = wrapper.getIntervenerOrganisation();
        return policy == null ? null : policy.getOrganisation().getOrganisationID();
    }
}
