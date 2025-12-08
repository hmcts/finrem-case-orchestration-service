package uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.HearingLike;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class HearingCorrespondenceHelper {

    private final PaperNotificationService paperNotificationService;
    private final ExpressCaseService expressCaseService;

    /**
     * PT todo - update tests
     * Retrieves the {@link Hearing} currently in context based on the working hearing ID from the case data.
     *
     * <p>This method accesses the {@link ManageHearingsWrapper} from the given {@link FinremCaseData},
     * and uses the working hearing ID, to locate the matching {@link Hearing} in the lis of hearings</p>
     *
     * <p>If the hearings list is {@code null}, or if no hearing matches the working hearing ID,
     * this method throws an {@link IllegalStateException} </p>
     *
     * <p>A working hearing refers to the {@link Hearing} a user is actively creating or modifying in the UI.</p>
     *
     * @param finremCaseData the case data containing the hearings and context
     * @return the {@link Hearing} associated with the current working hearing ID
     * @throws IllegalStateException if the hearings list is missing, or no matching hearing is found
     */
    public HearingLike getHearingInContext(FinremCaseData finremCaseData) {
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        if (ManageHearingsAction.ADD_HEARING.equals(manageHearingsWrapper.getManageHearingsActionSelection())) {
            UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

            List<ManageHearingsCollectionItem> hearings = manageHearingsWrapper.getHearings();

            if (hearings == null) {
                throw new IllegalStateException(
                    "No hearings available to search for. Working hearing ID is: " + hearingId
                );
            }

            return  manageHearingsWrapper.getHearings().stream()
                .filter(h -> hearingId.equals(h.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + hearingId))
                .getValue();
        }

        if (ManageHearingsAction.VACATE_HEARING.equals(finremCaseData.getManageHearingsWrapper().getManageHearingsActionSelection())) {
            UUID workingVacatedHearingId = manageHearingsWrapper.getWorkingVacatedHearingId();
            return Optional.ofNullable(manageHearingsWrapper.getVacatedOrAdjournedHearingsCollectionItemById(workingVacatedHearingId))
                .map(VacatedOrAdjournedHearingsCollectionItem::getValue)
                .orElseThrow(() -> new IllegalStateException("Vacated hearing not found for the given ID: " + workingVacatedHearingId));
        }

        return null;
    }

    /**
     * Retrieves the {@link HearingTabItem} currently in context based on the working hearing ID from the case data.
     *
     * <p>This method accesses the {@link ManageHearingsWrapper} from the provided {@link FinremCaseData},
     * and uses the working hearing ID to locate the matching {@link HearingTabItem} in the list of hearing tab items.</p>
     *
     * <p>If no item matches the working hearing ID,
     * this method throws an {@link IllegalStateException}.</p>
     *
     * <p>A working hearing refers to the {@link HearingTabItem} a user is actively creating or modifying in the UI.</p>
     *
     * @param finremCaseData the case data containing the hearing tab items and context
     * @return the {@link HearingTabItem} associated with the current working hearing ID
     * @throws IllegalStateException if the hearing tab items list is missing, or no matching item is found
     */
    public HearingTabItem getHearingInContextFromTab(FinremCaseData finremCaseData) {
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();
        UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

        return manageHearingsWrapper.getHearingTabItems().stream()
            .filter(h -> hearingId.equals(h.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Hearing Tab Item not found for the given ID: " + hearingId))
            .getValue();
    }

    /**
     * Determines if notifications should not be sent for a hearing.
     * Should return true if the hearing's notice prompt is set to NO or is NULL.
     * @param hearing The hearing to check.
     * @return true if notification is required, false otherwise.
     */
    public boolean shouldNotSendNotification(HearingLike hearing) {
        return YesOrNo.isNoOrNull(hearing.getHearingNoticePrompt());
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the applicant solicitor should receive an email notification.
     */
    public boolean shouldEmailToApplicantSolicitor(FinremCaseDetails finremCaseDetails) {
        return !paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails);
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the respondent solicitor should receive an email notification.
     */
    public boolean shouldEmailToRespondentSolicitor(FinremCaseDetails finremCaseDetails) {
        return !paperNotificationService.shouldPrintForRespondent(finremCaseDetails);
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the applicant should receive hearing documents by post.
     */
    public boolean shouldPostToApplicant(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForApplicantDisregardApplicationType(finremCaseDetails);
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the respondent should receive hearing documents by post.
     */
    public boolean shouldPostToRespondent(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForRespondent(finremCaseDetails);
    }

    /**
     * PT consider chage visibility to private.  Also the bit JT hated could be refactored easily
     * Determines if a hearing should only send a notice. And should NOT send additional hearing documents.
     * To return true:
     * - the HearingType must appear in the noticeOnlyHearingTypes set.
     * - FDR hearings are an exception, they're notice only when the case is NOT an express case.
     * @param finremCaseDetails case details
     * @param hearing the hearing to check
     * @return true if the hearing should only send a notice, false otherwise
     */
    public boolean isHearingThatOnlyNeedsNotice(FinremCaseDetails finremCaseDetails, HearingLike hearing) {
        Set<HearingType> noticeOnlyHearingTypes = Set.of(
            HearingType.MPS,
            HearingType.FH,
            HearingType.DIR,
            HearingType.MENTION,
            HearingType.PERMISSION_TO_APPEAL,
            HearingType.APPEAL_HEARING,
            HearingType.APPLICATION_HEARING,
            HearingType.RETRIAL_HEARING,
            HearingType.PTR
        );

        return Optional.ofNullable(hearing)
            .map(HearingLike::getHearingType)
            .map(hearingType ->
                noticeOnlyHearingTypes.contains(hearingType)
                    || (HearingType.FDR.equals(hearingType)
                    && !expressCaseService.isExpressCase(finremCaseDetails.getData()))
            )
            .orElse(false);
    }

    // PT todo, unit test
    /*
     * Some, newly added hearings, only send a hearing notice (and bulk cover sheet).
     * Returns true user is adding a new hearing and the hearing is the right type.
     * @param finremCaseDetails used to get action selection
     * @param hearing used to check the hearing itself
     */
    public boolean shouldPostHearingNoticeOnly(FinremCaseDetails finremCaseDetails, HearingLike hearing) {
        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);

        return isAddHearingAction(actionSelection)
            && isHearingThatOnlyNeedsNotice(finremCaseDetails, hearing);
    }

    // PT todo, unit test
    /*
     * Some, vacated and relisted hearings, only send a hearing and vacate notices (and bulk cover sheet).
     * Returns true user is vacating and relisting a hearing and the hearing is the right type.
     * @param finremCaseDetails used to get action selection
     * @param hearing used to check the hearing itself
     */
    public boolean shouldPostHearingAndVacateNotices(FinremCaseDetails finremCaseDetails, HearingLike hearing) {
        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);

        boolean hearingIsRelisted =
            YesOrNo.YES.equals(finremCaseDetails.getData().getManageHearingsWrapper().getWasRelistSelected());

        return isVacateHearingAction(actionSelection)
            && hearingIsRelisted
            && isHearingThatOnlyNeedsNotice(finremCaseDetails, hearing);
    }

    // PT todo, unit test
    /*
     * Some vacated hearings only send a vacate notice (and bulk cover sheet).
     * Returns true user is vacating a hearing and not relisting another hearing.
     * @param finremCaseDetails used to get action selection
     * @param hearing used to check the hearing itself
     */
    public boolean shouldPostVacateNoticeOnly(FinremCaseDetails finremCaseDetails) {
        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);

        boolean hearingNotRelisted =
            YesOrNo.NO.equals(finremCaseDetails.getData().getManageHearingsWrapper().getWasRelistSelected());

        return isVacateHearingAction(actionSelection) && hearingNotRelisted;
    }

    /**
     * Retrieves the action selection, e.g. ADD_HEARING, from the Manage Hearings Wrapper in the case details.
     * @param finremCaseDetails the case details containing the Manage Hearings Wrapper
     * @return the ManageHearingsAction or null if not present
     */
    public ManageHearingsAction getManageHearingsAction(FinremCaseDetails finremCaseDetails) {
        return Optional.ofNullable(finremCaseDetails)
            .map(FinremCaseDetails::getData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(ManageHearingsWrapper::getManageHearingsActionSelection)
            .orElse(null);
    }

    /*
     * PT todo: test
     * Returns true is a hearing was vacated and relisted
     * @param finremCaseDetails queried to see if the vacate action was chosen and if the hearing was relisted.
     */
    public boolean isVacatedAndRelistedHearing(FinremCaseDetails finremCaseDetails) {
        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);
        boolean hearingRelisted = YesOrNo.YES.equals(
            finremCaseDetails.getData().getManageHearingsWrapper().getWasRelistSelected());
        return isVacateHearingAction(actionSelection) && hearingRelisted;
    }

    /**
     * Determines if the action selection is to add a hearing.
     * @param actionSelection the action selection to check
     * @return true if the action selection is ADD_HEARING, false otherwise
     */
    private boolean isAddHearingAction(ManageHearingsAction actionSelection) {
        return ManageHearingsAction.ADD_HEARING.equals(actionSelection);
    }

    /**
     * Determines if the action selection is to vacate a hearing.
     * @param actionSelection the action selection to check
     * @return true if the action selection is VACATE_HEARING, false otherwise
     */
    private boolean isVacateHearingAction(ManageHearingsAction actionSelection) {
        return ManageHearingsAction.VACATE_HEARING.equals(actionSelection);
    }
}
