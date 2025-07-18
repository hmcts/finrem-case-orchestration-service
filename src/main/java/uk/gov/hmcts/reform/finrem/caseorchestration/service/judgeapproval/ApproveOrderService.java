package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApproveOrderService {

    private final JudgeApprovalResolver judgeApprovalResolver;

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

    /**
     * Populates judge decisions for draft orders by iterating through a predefined range of indexes (1 to 5),
     * resolving judge approvals, and updating the corresponding draft orders and PSA documents statuses and hearing instructions.
     *
     * <p>For each index, the method:
     * <ol>
     *   <li>Resolves the {@link JudgeApproval} object for the specified index.</li>
     *   <li>Validates the judge approval and ensures it contains a valid document.</li>
     *   <li>Updates the draft orders and PSA documents in the provided {@link DraftOrdersWrapper} based on the judge's decision.</li>
     * </ol>
     *
     * <p>After processing, the method builds a confirmation body reflecting the decisions made</p>
     *
     * @param draftOrdersWrapper the wrapper object containing draft orders and PSA document collections to be updated
     * @param userAuthorisation  the authorisation token of the user, used to fetch the approving judge's details
     */
    public void populateJudgeDecisions(FinremCaseDetails finremCaseDetails, DraftOrdersWrapper draftOrdersWrapper,
                                       String userAuthorisation) {

        for (int i = 1; i <= 5; i++) {
            JudgeApproval approval = resolveJudgeApproval(draftOrdersWrapper, i);
            if (approval == null) {
                continue;
            }
            processJudgeDecision(finremCaseDetails, draftOrdersWrapper, approval, userAuthorisation);
        }
        buildConfirmationBody(finremCaseDetails, draftOrdersWrapper);
    }

    private void processJudgeDecision(FinremCaseDetails caseDetails, DraftOrdersWrapper wrapper, JudgeApproval approval, String authToken) {
        ofNullable(approval.getDocument()).ifPresent(caseDocument -> judgeApprovalResolver.populateJudgeDecision(caseDetails, wrapper, caseDocument,
            approval, authToken));
    }

    /**
     * Resolves the {@link JudgeApproval} object from the provided {@link DraftOrdersWrapper}
     * based on the specified index. Each index corresponds to a specific judge approval field
     * in the wrapper. If the index is out of the supported range (1 to 5), the method returns null.
     *
     * <p>Mapping of index to fields:</p>
     * <ul>
     *     <li>1 - {@link DraftOrdersWrapper#getJudgeApproval1()}</li>
     *     <li>2 - {@link DraftOrdersWrapper#getJudgeApproval2()}</li>
     *     <li>3 - {@link DraftOrdersWrapper#getJudgeApproval3()}</li>
     *     <li>4 - {@link DraftOrdersWrapper#getJudgeApproval4()}</li>
     *     <li>5 - {@link DraftOrdersWrapper#getJudgeApproval5()}</li>
     * </ul>
     *
     * @param draftOrdersWrapper the {@link DraftOrdersWrapper} containing judge approval fields.
     * @param index              the index specifying which judge approval field to retrieve (1-5).
     * @return the corresponding {@link JudgeApproval} object, or null if the index is out of range.
     */
    public JudgeApproval resolveJudgeApproval(DraftOrdersWrapper draftOrdersWrapper, int index) {
        return switch (index) {
            case 1 -> draftOrdersWrapper.getJudgeApproval1();
            case 2 -> draftOrdersWrapper.getJudgeApproval2();
            case 3 -> draftOrdersWrapper.getJudgeApproval3();
            case 4 -> draftOrdersWrapper.getJudgeApproval4();
            case 5 -> draftOrdersWrapper.getJudgeApproval5();
            default -> null;
        };
    }

    /**
     * Builds the confirmation body for approved, changed, and reviewed draft orders and sets it in the {@link DraftOrdersWrapper}.
     *
     * <p>This method evaluates the judge's decision for up to five draft orders associated with the given case
     * and groups them into different categories based on the decision. These categories include:
     * <ul>
     *     <li>Orders ready to be sealed</li>
     *     <li>Orders requiring changes by the legal representative</li>
     *     <li>Orders requiring changes by the judge</li>
     *     <li>Orders marked for review later</li>
     * </ul>
     * After categorizing the orders, the method constructs a confirmation body with appropriate messages
     * and updates the {@link DraftOrdersWrapper} with this information.
     *
     * @param caseDetails        the case details containing information about the case
     * @param draftOrdersWrapper the wrapper object containing the draft orders and related data
     * @throws IllegalStateException if an unhandled judge decision is encountered
     */
    private void buildConfirmationBody(FinremCaseDetails caseDetails, DraftOrdersWrapper draftOrdersWrapper) {

        final List<String> ordersApproved = new ArrayList<>();
        final List<String> ordersRepresentativeChanges = new ArrayList<>();
        final List<String> ordersChanged = new ArrayList<>();
        final List<String> ordersReviewLater = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = resolveJudgeApproval(draftOrdersWrapper, i);

            ofNullable(judgeApproval).map(JudgeApproval::getDocument)
                .ifPresent(targetDoc -> captureFilenames(judgeApproval, ordersApproved, ordersRepresentativeChanges,
                    ordersChanged, ordersReviewLater));
        }

        StringBuilder body = new StringBuilder();
        if (CollectionUtils.isNotEmpty(ordersApproved)) {
            body.append(String.format(ORDERS_APPROVED_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersApproved)))
                .append("\n\n");
        }
        if (CollectionUtils.isNotEmpty(ordersRepresentativeChanges)) {
            body.append(String.format(ORDERS_REQUIRE_REPRESENTATIVE_CHANGE_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersRepresentativeChanges)))
                .append("\n\n");
        }
        if (CollectionUtils.isNotEmpty(ordersChanged)) {
            body.append(String.format(ORDERS_AMENDED_CHANGE_CONFIRMATION_BODY_FORMAT,
                    String.join(", ", ordersChanged)))
                .append("\n\n");
        }
        if (CollectionUtils.isNotEmpty(ordersReviewLater)) {
            body.append(String.format(ORDERS_REVIEW_LATER_CONFIRMATION_BODY_FORMAT,
                String.join(", ", ordersReviewLater), caseDetails.getId()));
        }

        draftOrdersWrapper.setApproveOrdersConfirmationBody(body.toString());
    }

    /**
     * Categorizes the filename of a document based on the judge's decision and adds it to the appropriate list.
     *
     * @param judgeApproval               the {@link JudgeApproval} object containing the document and the judge's decision.
     * @param ordersApproved              the list to which filenames of documents marked as "Ready to be sealed" will be added.
     * @param ordersRepresentativeChanges the list to which filenames of documents requiring changes by the legal representative will be added.
     * @param ordersChanged               the list to which filenames of documents requiring changes by the judge will be added.
     * @param ordersReviewLater           the list to which filenames of documents marked for review later will be added.
     * @throws IllegalStateException if the judge's decision is not handled.
     */
    private void captureFilenames(JudgeApproval judgeApproval, List<String> ordersApproved, List<String> ordersRepresentativeChanges,
                                  List<String> ordersChanged, List<String> ordersReviewLater) {
        String fileName = judgeApproval.getDocument().getDocumentFilename();

        switch (judgeApproval.getJudgeDecision()) {
            case READY_TO_BE_SEALED -> ordersApproved.add(fileName);
            case LEGAL_REP_NEEDS_TO_MAKE_CHANGE -> ordersRepresentativeChanges.add(fileName);
            case JUDGE_NEEDS_TO_MAKE_CHANGES -> ordersChanged.add(fileName);
            case REVIEW_LATER -> ordersReviewLater.add(fileName);
            default -> throw new IllegalStateException("Unhandled judge decision for document:" + fileName);
        }
    }
}
