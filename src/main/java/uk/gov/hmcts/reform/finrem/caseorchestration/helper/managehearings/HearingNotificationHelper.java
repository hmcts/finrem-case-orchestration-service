package uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class HearingNotificationHelper {

    private final ManageHearingsNotificationRequestMapper notificationRequestMapper;
    private final NotificationService notificationService;

    /**
     * Retrieves the hearing currently in context, based on the working hearing ID from the case data.
     *
     * <p>This method looks up the {@link ManageHearingsWrapper} from the provided case data,
     * extracts the working hearing ID, and finds the corresponding {@link Hearing} in the list of hearings.
     * If the hearing ID is not found, it throws an {@link IllegalStateException}.</p>
     *
     * <p>A working hearing is a {@link Hearing} that a user is currently creating or amending.</p>
     *
     * @param finremCaseData the case data containing hearing information
     * @return the {@link Hearing} associated with the working hearing ID
     * @throws IllegalStateException if no matching hearing is found for the working hearing ID
     */
    public Hearing getHearingInContext(FinremCaseData finremCaseData) {
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();
        UUID hearingId = manageHearingsWrapper.getWorkingHearingId();

        return manageHearingsWrapper.getHearings().stream()
                .filter(h -> h.getId().equals(hearingId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Hearing not found for the given ID: " + hearingId))
                .getValue();
    }

    /**
     * Determines if notifications should be sent for a hearing.
     * @param hearing The hearing to check.
     * @return true if notification is required, false otherwise.
     */
    public boolean shouldSendNotification(Hearing hearing) {
        return YesOrNo.YES.equals(hearing.getHearingNoticePrompt());
    }

    /**
     * Sends a hearing notification to the party specified in parameters.
     *
     * <p>Uses the {@link CaseRole} of the specified party to decide what to send.
     *
     * @param party the dynamic multi-select list element for the party
     * @param finremCaseDetails the case details with detail needed in generating the notification
     * @param hearing the hearing associated with the notification
     */
    public void sendHearingNotificationsByParty(DynamicMultiSelectListElement party,
                                                FinremCaseDetails finremCaseDetails,
                                                Hearing hearing) {
        CaseRole caseRole = CaseRole.forValue(party.getCode());
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR ->
                sendHearingNotificationToApplicantSolicitor(
                        finremCaseDetails,
                        hearing);
            case CaseRole.RESP_SOLICITOR ->
                log.info("Handling case: RESP_SOLICITOR, work to follow");
            case CaseRole.INTVR_SOLICITOR_1 ->
                log.info("Handling case: INTVR_SOLICITOR_1, work to follow");
            case CaseRole.INTVR_SOLICITOR_2 ->
                log.info("Handling case: INTVR_SOLICITOR_2, work to follow");
            case CaseRole.INTVR_SOLICITOR_3 ->
                log.info("Handling case: INTVR_SOLICITOR_3, work to follow");
            case CaseRole.INTVR_SOLICITOR_4 ->
                log.info("Handling case: INTVR_SOLICITOR_4, work to follow");
            default -> throw new IllegalStateException(
                    String.format(
                            "Unexpected value: %s for case reference %s",
                            caseRole,
                            finremCaseDetails.getId()
                    )
            );
        }
    }

    /**
     * Builds and sends a hearing notification to the applicant's solicitor.
     *
     * <p>This method uses the {@link ManageHearingsNotificationRequestMapper} to create a {@link NotificationRequest}
     * based on the case details and hearing provided. It then delegates the actual sending of the notification
     * to the {@link NotificationService}.</p>
     *
     * @param finremCaseDetails the case details containing relevant information about the hearing and case participants
     * @param hearing the hearing for which the notification is being sent
     */
    public void sendHearingNotificationToApplicantSolicitor(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing) {

        NotificationRequest notificationRequest = notificationRequestMapper
                .buildHearingNotificationForApplicantSolicitor(finremCaseDetails, hearing);

        notificationService.sendHearingNotificationToApplicant(notificationRequest);
    }
}
