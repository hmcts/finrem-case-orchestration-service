package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {
    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;

    /**
     * Processes approved orders for a financial remedy case.
     * This method generates and stores the contested order approved letter,
     * creates additional order documents, appends the latest draft direction order,
     * and updates the hearing order collection with approved hearing orders.
     *
     * @param caseDetails       the current state of the financial remedy case
     * @param authorisationToken the authorisation token for accessing secure resources
     */
    public void processApprovedOrdersMh(FinremCaseDetails caseDetails, String authorisationToken) {
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);

        processCaseworkerUploadedApprovedOrders(caseDetails, authorisationToken);

        // TODO Looks like the following logic is not needed anymore. Remove it later.
        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
    }

    /**
     * Clears the Caseworker Approved Order collection from the given {@link FinremCaseData}.
     *
     * <p>
     * This method sets the {@code cwApprovedOrderCollection} field within the
     * {@code DraftDirectionWrapper} to {@code null}, effectively removing
     * all caseworker-approved orders from the case data.
     *
     * @param caseData the {@link FinremCaseData} object whose caseworker-approved
     *                 order collection should be cleared
     */
    public void clearCwApprovedOrderCollection(FinremCaseData caseData) {
        caseData.getDraftDirectionWrapper().setCwApprovedOrderCollection(null);
    }

    private void processCaseworkerUploadedApprovedOrders(FinremCaseDetails caseDetails, String authorisationToken) {
        hearingOrderService.stampAndStoreCwApprovedOrders(caseDetails, authorisationToken);
    }
}
