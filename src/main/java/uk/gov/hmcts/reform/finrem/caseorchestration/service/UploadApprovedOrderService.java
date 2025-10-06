package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {
    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    /**
     * Method for processing approved orders in a financial remedy case.
     * This method generates and stores the contested order approved letter, creates additional order documents,
     * appends the latest draft direction order, and updates the hearing order collection with approved hearing orders.
     *
     * <p>Use {@link #processApprovedOrdersMh(FinremCaseDetails, String)} instead.</p>
     *
     * @param callbackRequest   the callback request containing case details
     * @param authorisationToken the authorisation token for accessing secure resources
     * @deprecated This method is deprecated and should not be used. Scheduled for removal since 30/09/2025.
     */
    @Deprecated(forRemoval = true, since = "30/09/2025")
    @SuppressWarnings("squid:S1133") // Suppress SonarQube rule for deprecated code
    public void processApprovedOrders(FinremCallbackRequest callbackRequest,
                                      String authorisationToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);

        processCaseworkerUploadedApprovedOrders(caseDetails.getData(), authorisationToken);

        // TODO Looks like the following logic is not needed. Remove it later.
        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, authorisationToken);
        }

        clearCwApprovedOrderCollection(caseDetails.getData());
    }

    /**
     * Determines if another hearing needs to be listed based on the case details.
     * This method is excluded from SonarQube scans.
     *
     * @param caseDetails the details of the financial remedy case
     * @return {@code true} if another hearing needs to be listed, {@code false} otherwise
     * @deprecated This method is deprecated and should not be used.
     */
    @Deprecated(forRemoval = true, since = "18/08/2025")
    @SuppressWarnings("squid:S1133") // Suppress SonarQube rule for deprecated code
    private boolean isAnotherHearingToBeListed(FinremCaseDetails caseDetails) {
        FinremCaseData data = caseDetails.getData();
        Optional<List<HearingDirectionDetailsCollection>> latestHearingDirections = Optional.ofNullable(data.getHearingDirectionDetailsCollection());
        if (latestHearingDirections.isPresent()) {
            List<HearingDirectionDetailsCollection> directionDetailsCollections = latestHearingDirections.get();
            if (!directionDetailsCollections.isEmpty()) {
                HearingDirectionDetailsCollection hearingCollection = directionDetailsCollections.getLast();
                return YesOrNo.YES.equals(hearingCollection.getValue().getIsAnotherHearingYN());
            }
        }
        return false;
    }

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

        processCaseworkerUploadedApprovedOrders(caseDetails.getData(), authorisationToken);

        // TODO Looks like the following logic is not needed anymore. Remove it later.
        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        clearCwApprovedOrderCollection(caseDetails.getData());
    }

    private void clearCwApprovedOrderCollection(FinremCaseData caseData) {
        caseData.getDraftDirectionWrapper().setCwApprovedOrderCollection(null);
    }

    private void processCaseworkerUploadedApprovedOrders(FinremCaseData caseData, String authorisationToken) {
        hearingOrderService.stampAndStoreCwApprovedOrders(caseData, authorisationToken);
    }
}
