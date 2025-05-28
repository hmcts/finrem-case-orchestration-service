package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;


@Service
@Slf4j
@RequiredArgsConstructor
public class ManageHearingsNotificationRequestMapper {

    /**
     * todo
     * templatevars handles the linkToSmartSurvey, courtname, courtEmail
     */
    public NotificationRequest buildHearingNotificationForApplicantSolicitor(
            DynamicMultiSelectListElement party,
            FinremCaseDetails finremCaseDetails,
            Hearing hearing)
    {

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        String applicantSurname = finremCaseData.getContactDetailsWrapper().getApplicantLname();
        String respondentSurname = finremCaseData.getContactDetailsWrapper().getRespondentLname();

        return NotificationRequest.builder()
            .notificationEmail(finremCaseData.getAppSolicitorEmail())
            .caseReferenceNumber(String.valueOf(finremCaseDetails.getId()))
            .hearingType(hearing.getHearingType().getId())
            .solicitorReferenceNumber(nullToEmpty(finremCaseData.getContactDetailsWrapper().getSolicitorReference()))
            .applicantName(applicantSurname)
            .respondentName(respondentSurname)
            .name(nullToEmpty(finremCaseData.getAppSolicitorName()))
            .build();
    }
}
