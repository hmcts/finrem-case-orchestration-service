package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageHearingsNotificationRequestMapper {

    private record PartySpecificDetails(
        String recipientEmailAddress,
        String recipientName
    ) {}

    /**
     * Constructs a {@link NotificationRequest} for sending a hearing notification to the applicant's solicitor.
     *
     * <p>Gets party specific details for an applicant, then passes to
     * buildHearingNotificationForParty to fill in the rest from data common to parties. </p>
     *
     * @param finremCaseDetails the case details including case ID, type, and applicant/solicitor info
     * @param hearing the hearing information
     * @return a fully constructed {@link NotificationRequest}
     */
    public NotificationRequest buildHearingNotificationForApplicantSolicitor(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing) {

        PartySpecificDetails partySpecificDetails = new PartySpecificDetails(
                finremCaseDetails.getData().getAppSolicitorEmail(),
                nullToEmpty(finremCaseDetails.getData().getAppSolicitorName())
        );

        return buildHearingNotificationForParty(finremCaseDetails, hearing, partySpecificDetails);
    }

    /**
     * Constructs a {@link NotificationRequest} for sending a hearing notification to the respondent's solicitor.
     *
     * <p>Gets party specific details, then passes to
     * buildHearingNotificationForParty to fill in the rest from data common to parties. </p>
     *
     * @param finremCaseDetails the case details including case ID, type, and party solicitor info
     * @param hearing the hearing information
     * @return a fully constructed {@link NotificationRequest}
     */
    public NotificationRequest buildHearingNotificationForRespondentSolicitor(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing) {

        PartySpecificDetails partySpecificDetails = new PartySpecificDetails(
                finremCaseDetails.getData().getRespondentSolicitorEmailForContested(),
                nullToEmpty(finremCaseDetails.getData().getRespondentSolicitorName())
        );

        return buildHearingNotificationForParty(finremCaseDetails, hearing, partySpecificDetails);
    }

    /**
     * Work in progress. Intervener bug means interveners never on the party list.
     * @return NotificationRequest for the intervener specified in the CaseRole.
     */
    public NotificationRequest buildHearingNotificationForIntervenerSolicitor(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing) {

        // Expect this to be something like
        // finremCaseDetails.getData().getIntervenerOne().getIntervenerSolEmail(),
        // finremCaseDetails.getData().getIntervenerOne().getIntervenerSolName()
        PartySpecificDetails partySpecificDetails = new PartySpecificDetails(
                "hardcodedinterveneremail@mailinator.com",
                "hard coded intervener name"
        );

        return buildHearingNotificationForParty(finremCaseDetails, hearing, partySpecificDetails);
    }

    /**
     * Builds a {@link NotificationRequest} for a hearing notification.  Uses common data from the case and
     * party specific details.
     *
     * @param finremCaseDetails  case details.
     * @param hearing The hearing in context.
     * @param partySpecificDetails include details for a specific party (applicant, respondent, or intervener).
     * @return NotificationRequest to the calling public method.
     */
    private NotificationRequest buildHearingNotificationForParty(
            FinremCaseDetails finremCaseDetails,
            Hearing hearing,
            PartySpecificDetails partySpecificDetails) {

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        String applicantSurname = finremCaseData.getContactDetailsWrapper().getApplicantLname();
        String respondentSurname = finremCaseData.getContactDetailsWrapper().getRespondentLname();

        String emailServiceCaseType = CaseType.CONTESTED.equals(finremCaseDetails.getCaseType())
            ? EmailService.CONTESTED : EmailService.CONSENTED;

        String selectedFRC  = CourtHelper.getFRCForHearing(hearing);

        return NotificationRequest.builder()
                .notificationEmail(partySpecificDetails.recipientEmailAddress)
                .caseReferenceNumber(String.valueOf(finremCaseDetails.getId()))
                .hearingType(hearing.getHearingType().getId())
                .solicitorReferenceNumber(nullToEmpty(finremCaseData.getContactDetailsWrapper().getSolicitorReference()))
                .applicantName(applicantSurname)
                .respondentName(respondentSurname)
                .name(partySpecificDetails.recipientName)
                .caseType(emailServiceCaseType)
                .selectedCourt(selectedFRC)
                .build();
    }
}
