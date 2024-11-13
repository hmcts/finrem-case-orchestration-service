package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrdersNotificationRequestMapper {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");


    public NotificationRequest buildJudgeNotificationRequest(FinremCaseDetails caseDetails, LocalDate date, String judge) {
        FinremCaseData caseData = caseDetails.getData();

        NotificationRequest judgeNotificationRequest = new NotificationRequest();
        judgeNotificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        judgeNotificationRequest.setHearingDate(dateFormatter.format(date));
        judgeNotificationRequest.setNotificationEmail(judge);
        judgeNotificationRequest.setApplicantName(Objects.toString(caseData.getFullApplicantName()));
        judgeNotificationRequest.setRespondentName(caseDetails.getData().getRespondentFullName());

        return judgeNotificationRequest;
    }
}
