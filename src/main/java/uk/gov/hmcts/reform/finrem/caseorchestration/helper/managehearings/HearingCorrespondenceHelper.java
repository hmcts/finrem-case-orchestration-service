package uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
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
    public Hearing getHearingInContext(FinremCaseData finremCaseData) {
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();
        UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

        List<ManageHearingsCollectionItem> hearings = manageHearingsWrapper.getHearings();

        if (hearings == null) {
            throw new IllegalStateException(
                    "No hearings available to search for. Working hearing ID is: " + hearingId
            );
        }

        return manageHearingsWrapper.getHearings().stream()
                .filter(h -> hearingId.equals(h.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + hearingId))
                .getValue();
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
    public boolean shouldNotSendNotification(Hearing hearing) {
        return YesOrNo.isNoOrNull(hearing.getHearingNoticePrompt());
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the applicant solicitor should receive an email notification.
     */
    public boolean shouldEmailToApplicantSolicitor(FinremCaseDetails finremCaseDetails) {
        return !paperNotificationService.shouldPrintForApplicant(finremCaseDetails);
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
        return paperNotificationService.shouldPrintForApplicant(finremCaseDetails);
    }

    /**
     * Wraps {@link PaperNotificationService} logic for readability.
     * @return true if the respondent should receive hearing documents by post.
     */
    public boolean shouldPostToRespondent(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForRespondent(finremCaseDetails);
    }

    /**
     * Determines if a hearing should only send a notice. And should NOT send additional hearing documents.
     * To return true:
     * - the Action must be ADD_HEARING.
     * - the HearingType must appear in the noticeOnlyHearingTypes set.
     * - FDR hearings are an exception, they're notice only when the case is NOT an express case.
     * @param finremCaseDetails case details
     * @param hearing the hearing to check
     * @return true if the hearing should only send a notice, false otherwise
     */
    public boolean shouldPostHearingNoticeOnly(FinremCaseDetails finremCaseDetails, Hearing hearing) {
        Set<HearingType> noticeOnlyHearingTypes = Set.of(
            HearingType.MPS,
            HearingType.FH,
            HearingType.DIR,
            HearingType.MENTION,
            HearingType.PERMISSION_TO_APPEAL,
            HearingType.APPEAL_HEARING,
            HearingType.RETRIAL_HEARING,
            HearingType.PTR
        );

        boolean isNoticeOnlyHearingType = Optional.ofNullable(hearing)
            .map(Hearing::getHearingType)
            .map(hearingType ->
                noticeOnlyHearingTypes.contains(hearingType)
                    || (HearingType.FDR.equals(hearingType)
                    && !expressCaseService.isExpressCase(finremCaseDetails.getData()))
            )
            .orElse(false);

        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);

        return isAddHearingAction(actionSelection) && isNoticeOnlyHearingType;
    }

    /**
     * Determines if a hearing should send a full set of hearing documents (not just a notice).
     * To return true:
     * - the Action must be ADD_HEARING.
     * - the HearingType must appear in the hearingTypesThatNeedDocumentsPosted set.
     * FDR hearings are an exception, all hearing documents are posted when the case is an express case only.
     * @param finremCaseDetails case details
     * @param hearing the hearing to check
     * @return true if the hearing should only send a notice, false otherwise
     */
    public boolean shouldPostAllHearingDocuments(FinremCaseDetails finremCaseDetails, Hearing hearing) {

        boolean allDocumentsNeedPosting = Optional.ofNullable(hearing)
            .map(Hearing::getHearingType)
            .map(hearingType ->
                hearingType == HearingType.FDA
                    || (hearingType == HearingType.FDR && expressCaseService.isExpressCase(finremCaseDetails.getData()))
            )
            .orElse(false);

        ManageHearingsAction actionSelection = getManageHearingsAction(finremCaseDetails);

        return isAddHearingAction(actionSelection) && allDocumentsNeedPosting;
    }

    /**
     * Retrieves the action selection, e.g. ADD_HEARING, from the Manage Hearings Wrapper in the case details.
     * @param finremCaseDetails the case details containing the Manage Hearings Wrapper
     * @return the ManageHearingsAction or null if not present
     */
    private ManageHearingsAction getManageHearingsAction(FinremCaseDetails finremCaseDetails) {
        return Optional.ofNullable(finremCaseDetails)
            .map(FinremCaseDetails::getData)
            .map(FinremCaseData::getManageHearingsWrapper)
            .map(ManageHearingsWrapper::getManageHearingsActionSelection)
            .orElse(null);
    }

    /**
     * Determines if the action selection is to add a hearing.
     * @param actionSelection the action selection to check
     * @return true if the action selection is ADD_HEARING, false otherwise
     */
    private boolean isAddHearingAction(ManageHearingsAction actionSelection) {
        return ManageHearingsAction.ADD_HEARING.equals(actionSelection);
    }
}
