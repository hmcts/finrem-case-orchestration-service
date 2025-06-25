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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// todo - corresponence helper?
// still think more lives here than it should - should be in corresponser.

@RequiredArgsConstructor
@Slf4j
@Component
public class HearingNotificationHelper {

    private final PaperNotificationService paperNotificationService;

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
                .filter(h -> h.getId().equals(hearingId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + hearingId))
                .getValue();
    }

    /**
     * Determines if notifications should not be sent for a hearing.
     * Should return true if the hearing's notice prompt is set to NO or is NULL.
     * @param hearing The hearing to check.
     * @return true if notification is required, false otherwise.
     */
    public boolean shouldNotSendNotification(Hearing hearing) {
        return !YesOrNo.YES.equals(hearing.getHearingNoticePrompt());
    }


    // todo? all todos need a test too
    public boolean emailingToApplicantSolicitor(FinremCaseDetails finremCaseDetails) {
        return !paperNotificationService.shouldPrintForApplicant(finremCaseDetails);
    }

    // todo?
    public boolean postingToApplicant(FinremCaseDetails finremCaseDetails) {
        return paperNotificationService.shouldPrintForApplicant(finremCaseDetails);
    }

    // todo? move to hearing class?  flip to FDA/FDR?
    public boolean shouldSendHearingNoticeOnly(FinremCaseDetails finremCaseDetails, Hearing hearing) {

        Set<HearingType> NOTICE_ONLY_HEARING_TYPES = Set.of(
                HearingType.MPS,
                HearingType.FH,
                HearingType.DIR,
                HearingType.MENTION,
                HearingType.PERMISSION_TO_APPEAL,
                HearingType.APPEAL_HEARING,
                HearingType.RETRIAL_HEARING,
                HearingType.PTR
        );

        ManageHearingsAction actionSelection = Optional.ofNullable(finremCaseDetails)
                .map(FinremCaseDetails::getData)
                .map(FinremCaseData::getManageHearingsWrapper)
                .map(ManageHearingsWrapper::getManageHearingsActionSelection)
                .orElse(null);

        boolean isAddHearingEvent = ManageHearingsAction.ADD_HEARING.equals(actionSelection);

        boolean isNoticeOnlyHearingType = Optional.ofNullable(hearing)
                .map(Hearing::getHearingType)
                .map(NOTICE_ONLY_HEARING_TYPES::contains)
                .orElse(false);

        return isAddHearingEvent && isNoticeOnlyHearingType;
    }
}
