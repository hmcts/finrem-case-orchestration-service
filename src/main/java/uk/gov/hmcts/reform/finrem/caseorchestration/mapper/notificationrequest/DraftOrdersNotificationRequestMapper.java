package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.RefusedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftOrdersNotificationRequestMapper {
    private final CourtDetailsConfiguration courtDetailsConfiguration;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public NotificationRequest buildJudgeNotificationRequest(FinremCaseDetails caseDetails, LocalDate hearingDate, String judge) {
        NotificationRequest judgeNotificationRequest = new NotificationRequest();
        judgeNotificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        judgeNotificationRequest.setHearingDate(dateFormatter.format(hearingDate));
        judgeNotificationRequest.setNotificationEmail(judge);
        judgeNotificationRequest.setApplicantName(caseDetails.getData().getFullApplicantName());
        judgeNotificationRequest.setRespondentName(caseDetails.getData().getRespondentFullName());

        return judgeNotificationRequest;
    }

    /**
     * Builds a {@link NotificationRequest} for a draft order ready for review notification that is to be sent to admin.
     *
     * @param caseDetails the case details
     * @param hearingDate the hearing date that the draft order is associated with
     * @return a {@link NotificationRequest} containing all required notification placeholder data
     */
    public NotificationRequest buildAdminReviewNotificationRequest(FinremCaseDetails caseDetails, LocalDate hearingDate) {
        FinremCaseData caseData = caseDetails.getData();
        String email = getCourtAdminEmail(caseData);

        return NotificationRequest.builder()
            .notificationEmail(email)
            .caseReferenceNumber(String.valueOf(caseDetails.getId()))
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .hearingDate(dateFormatter.format(hearingDate))
            .build();
    }

    public NotificationRequest buildCaseworkerDraftOrderReviewOverdue(FinremCaseDetails caseDetails,
                                                                      DraftOrdersReview draftOrdersReview) {
        FinremCaseData caseData = caseDetails.getData();
        String notificationEmail = getCourtAdminEmail(caseData);

        return NotificationRequest.builder()
            .notificationEmail(notificationEmail)
            .caseReferenceNumber(String.valueOf(caseDetails.getId()))
            .caseType(EmailService.CONTESTED)
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .hearingDate(dateFormatter.format(draftOrdersReview.getHearingDate()))
            .selectedCourt(CourtHelper.getSelectedFrc(caseDetails))
            .judgeName(draftOrdersReview.getHearingJudge())
            .oldestDraftOrderDate(dateFormatter.format(draftOrdersReview.getEarliestToBeReviewedOrderDate()))
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for a refused draft order or Pension Sharing Annex (PSA).
     *
     * @param caseDetails the case details containing case and party information.
     * @param refusedOrder the refused order details, including the reason and associated document.
     * @return a {@link NotificationRequest} containing all required notification details.
     * @throws IllegalArgumentException if the draft order or PSA document filename is not available.
     */
    public NotificationRequest buildRefusedDraftOrderOrPsaNotificationRequest(FinremCaseDetails caseDetails, RefusedOrder refusedOrder) {
        FinremCaseData caseData = caseDetails.getData();
        String notificationEmail = getRefusedOrderEmail(caseDetails, refusedOrder);
        String documentName = ofNullable(refusedOrder.getRefusedDocument()).map(CaseDocument::getDocumentFilename)
            .orElseThrow(IllegalArgumentException::new);

        return NotificationRequest.builder()
            .notificationEmail(notificationEmail)
            .caseReferenceNumber(String.valueOf(caseDetails.getId()))
            .caseType(EmailService.CONTESTED)
            .applicantName(caseData.getFullApplicantName())
            .respondentName(caseData.getRespondentFullName())
            .hearingDate(dateFormatter.format(refusedOrder.getHearingDate()))
            .selectedCourt(CourtHelper.getSelectedFrc(caseDetails))
            .judgeName(refusedOrder.getRefusalJudge())
            .judgeFeedback(refusedOrder.getJudgeFeedback())
            .documentName(documentName)
            .solicitorReferenceNumber(nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference()))
            .name(getRefusedOrderName(caseDetails, refusedOrder))
            .build();
    }

    private String getCourtAdminEmail(FinremCaseData caseData) {
        String selectedAllocatedCourt = caseData.getSelectedAllocatedCourt();
        return courtDetailsConfiguration.getCourts().get(selectedAllocatedCourt).getEmail();
    }

    private String getRefusedOrderEmail(FinremCaseDetails caseDetails, RefusedOrder refusedOrder) {
        // If the refused order does not have a submitted by email then it was uploaded by a
        // caseworker on behalf of either the applicant or respondent.
        return Optional.ofNullable(refusedOrder.getSubmittedByEmail())
            .orElseGet(() -> getRefusedOrderSolicitorEmail(caseDetails, refusedOrder));
    }

    private String getRefusedOrderSolicitorEmail(FinremCaseDetails caseDetails, RefusedOrder refusedOrder) {
        FinremCaseData caseData = caseDetails.getData();
        return switch (refusedOrder.getOrderFiledBy()) {
            case APPLICANT -> caseData.getContactDetailsWrapper().getApplicantSolicitorEmail();
            case RESPONDENT -> caseData.getContactDetailsWrapper().getRespondentSolicitorEmail();
            default -> throw new IllegalArgumentException("Case ID " + caseDetails.getId()
                + ": No refused order recipient email address found");
        };
    }

    private String getRefusedOrderName(FinremCaseDetails caseDetails, RefusedOrder refusedOrder) {
        if (refusedOrder.getSubmittedByEmail() != null) {
            return refusedOrder.getSubmittedBy();
        } else {
            // If the refused order does not have a submitted by email then it was uploaded by a
            // caseworker on behalf of either the applicant or respondent.
            FinremCaseData caseData = caseDetails.getData();
            return switch (refusedOrder.getOrderFiledBy()) {
                case APPLICANT -> caseData.getContactDetailsWrapper().getApplicantSolicitorName();
                case RESPONDENT -> caseData.getContactDetailsWrapper().getRespondentSolicitorName();
                default -> throw new IllegalArgumentException("Case ID " + caseDetails.getId()
                    + ": No refused order recipient name found");
            };
        }
    }
}
