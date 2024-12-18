package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class JudgeApprovalInfoCapturer {

    private final JudgeApprovalHelper judgeApprovalHelper;

    private static final String ORDERS_APPROVED_CONFIRMATION_BODY_FORMAT =
        "<br>The draft orders (%s) for the case have been approved. "
            + "They are now ready for caseworker review.";

    private static final String ORDERS_REQUIRE_REPRESENTATIVE_CHANGE_CONFIRMATION_BODY_FORMAT =
        "<br>You have said that the legal representative needs to make some changes to the draft order(s). "
            + "They have been sent an email with your reasons for rejecting the orders (%s)";

    private static final String ORDERS_AMENDED_CHANGE_CONFIRMATION_BODY_FORMAT =
        "<br>You have amended the draft orders (%s). They are now ready for caseworker review";

    private static final String ORDERS_REVIEW_LATER_CONFIRMATION_BODY_FORMAT =
        "<br>You have said you will review draft orders (%s) later. These will remain on the "
            + "['Draft Orders' tab](/cases/case-details/%s#Draft%%20orders).";

    void buildConfirmationBody(FinremCaseDetails caseDetails, DraftOrdersWrapper draftOrdersWrapper) {

        final List<String> ordersApproved = new ArrayList<>();
        final List<String> ordersRepresentativeChanges = new ArrayList<>();
        final List<String> ordersChanged = new ArrayList<>();
        final List<String> ordersReviewLater = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = judgeApprovalHelper.resolveJudgeApproval(draftOrdersWrapper, i);
            String fileName = judgeApproval.getDocument().getDocumentFilename();
            switch (judgeApproval.getJudgeDecision()) {
                case READY_TO_BE_SEALED -> ordersApproved.add(fileName);
                case LEGAL_REP_NEEDS_TO_MAKE_CHANGE -> ordersRepresentativeChanges.add(fileName);
                case JUDGE_NEEDS_TO_MAKE_CHANGES -> ordersChanged.add(fileName);
                case REVIEW_LATER -> ordersReviewLater.add(fileName);
                default -> throw new IllegalStateException("Unhandled judge decision for document:" + fileName);
            }
        }

        StringBuilder body = new StringBuilder();
        if (!ordersApproved.isEmpty()) {
            body.append(String.format(ORDERS_APPROVED_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersApproved)))
                .append("\n\n");
        }
        if (!ordersRepresentativeChanges.isEmpty()) {
            body.append(String.format(ORDERS_REQUIRE_REPRESENTATIVE_CHANGE_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersRepresentativeChanges)))
                .append("\n\n");
        }
        if (!ordersChanged.isEmpty()) {
            body.append(String.format(ORDERS_AMENDED_CHANGE_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersChanged)))
                .append("\n\n");
        }
        if (!ordersReviewLater.isEmpty()) {
            body.append(String.format(ORDERS_REVIEW_LATER_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersReviewLater), caseDetails.getId()));
        }

        draftOrdersWrapper.setApproveOrdersConfirmationBody(body.toString());
    }
}