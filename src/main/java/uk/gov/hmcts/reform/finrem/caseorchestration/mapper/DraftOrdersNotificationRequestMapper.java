package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrdersNotificationRequestMapper {

    private final HearingService hearingService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");


    public NotificationRequest buildJudgeNotificationRequest(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        UploadAgreedDraftOrder agreedDraftOrder = caseData.getDraftOrdersWrapper().getUploadAgreedDraftOrder();
        LocalDate date = hearingService.getHearingDate(caseData, agreedDraftOrder.getHearingDetails().getValue());

        NotificationRequest judgeNotificationRequest = new NotificationRequest();
        judgeNotificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        judgeNotificationRequest.setHearingDate(dateFormatter.format(date));
        judgeNotificationRequest.setNotificationEmail(agreedDraftOrder.getJudge());
        judgeNotificationRequest.setApplicantName(Objects.toString(caseData.getFullApplicantName()));
        judgeNotificationRequest.setRespondentName(caseDetails.getData().getRespondentFullName());

        return judgeNotificationRequest;
    }
}
