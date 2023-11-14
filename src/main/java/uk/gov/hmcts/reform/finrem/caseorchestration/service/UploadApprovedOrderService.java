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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
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


        List<UploadAdditionalDocumentCollection> uploadAdditionalDocumentBefore
            = caseDetailsBefore.getData().getUploadAdditionalDocument();

        if (uploadAdditionalDocumentBefore != null && !uploadAdditionalDocumentBefore.isEmpty()) {
            List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = caseData.getUploadAdditionalDocument();
            if (uploadAdditionalDocument != null && !uploadAdditionalDocument.isEmpty()) {
                uploadAdditionalDocumentBefore.addAll(uploadAdditionalDocument);
                caseData.setUploadAdditionalDocument(uploadAdditionalDocumentBefore);
            }
        }
    }

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
}
