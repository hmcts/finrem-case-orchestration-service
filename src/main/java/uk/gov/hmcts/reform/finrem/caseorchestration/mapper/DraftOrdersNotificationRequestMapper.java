package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrdersNotificationRequestMapper {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private final CourtDetailsConfiguration courtDetailsConfiguration;

    public NotificationRequest buildCaseworkerDraftOrderReviewOverdue(FinremCaseDetails caseDetails,
                                                                      DraftOrdersReview draftOrdersReview) {
        FinremCaseData caseData = caseDetails.getData();
        String selectedAllocatedCourt = caseDetails.getData().getSelectedAllocatedCourt();
        String notificationEmail = courtDetailsConfiguration.getCourts().get(selectedAllocatedCourt).getEmail();

        return NotificationRequest.builder()
            .notificationEmail(notificationEmail)
            .caseReferenceNumber(String.valueOf(caseDetails.getId()))
            .caseType(EmailService.CONTESTED)
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .hearingDate(dateFormatter.format(draftOrdersReview.getHearingDate()))
            .selectedCourt(ContestedCourtHelper.getSelectedFrc(caseDetails))
            .judgeName(draftOrdersReview.getHearingJudge())
            .oldestDraftOrderDate(dateFormatter.format(draftOrdersReview.getEarliestToBeReviewedOrderDate()))
            .build();
    }
}
