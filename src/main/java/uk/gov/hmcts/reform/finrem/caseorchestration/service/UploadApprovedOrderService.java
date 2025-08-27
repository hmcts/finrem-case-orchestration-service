package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
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
    private final ContestedOrderApprovedLetterService letterService;
    private final AdditionalHearingDocumentService documentService;
    private final ApprovedOrderNoticeOfHearingService noticeService;

    /**
     * Method for processing approved orders in a financial remedy case.
     * This method generates and stores the contested order approved letter, creates additional order documents,
     * appends the latest draft direction order, and updates the hearing order collection with approved hearing orders.
     *
     * <p>Use {@link #processApprovedOrdersMh(FinremCaseDetails, FinremCaseDetails, String)} instead.</p>
     *
     * @param callbackRequest   the callback request containing case details
     * @param errors            a list to collect error messages encountered during processing
     * @param authorisationToken the authorisation token for accessing secure resources
     * @deprecated This method is deprecated and should not be used. Scheduled for removal since 30/09/2025.
     */
    @Deprecated(forRemoval = true, since = "30/09/2025")
    @SuppressWarnings("squid:S1133") // Suppress SonarQube rule for deprecated code
    public void processApprovedOrders(FinremCallbackRequest callbackRequest,
                                      List<String> errors,
                                      String authorisationToken) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        letterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);
        try {
            documentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
        }

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        if (isAnotherHearingToBeListed(caseDetails)) {
            noticeService.createAndStoreHearingNoticeDocumentPack(caseDetails, authorisationToken);
        }

        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        List<DirectionOrderCollection> hearingOrderCollectionBefore
            = documentService.getApprovedHearingOrders(caseDetailsBefore, authorisationToken);

        FinremCaseData caseData = caseDetails.getData();
        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        hearingOrderCollectionBefore.addAll(uploadHearingOrders);
        caseData.setUploadHearingOrder(hearingOrderCollectionBefore);
        documentService.addToFinalOrderCollection(caseDetails, authorisationToken);
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
                HearingDirectionDetailsCollection hearingCollection = directionDetailsCollections.get(directionDetailsCollections.size() - 1);
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
     * @param detailsBefore     the previous state of the financial remedy case
     * @param authorisationToken the authorisation token for accessing secure resources
     */
    public void processApprovedOrdersMh(FinremCaseDetails caseDetails, FinremCaseDetails detailsBefore, String authorisationToken) {
        letterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);
        documentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(authorisationToken, caseDetails);
        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DirectionOrderCollection> hearingOrderCollectionBefore
            = documentService.getApprovedHearingOrders(detailsBefore, authorisationToken);

        FinremCaseData caseData = caseDetails.getData();
        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        hearingOrderCollectionBefore.addAll(uploadHearingOrders);
        caseData.setUploadHearingOrder(hearingOrderCollectionBefore);
        documentService.addToFinalOrderCollection(caseDetails, authorisationToken);
    }
}
