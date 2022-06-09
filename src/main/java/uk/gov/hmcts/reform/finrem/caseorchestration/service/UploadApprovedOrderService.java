package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_DIRECTION_ORDER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {

    private final HearingOrderService hearingOrderService;
    private final CaseDataService caseDataService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    public Map<String, Object> handleLatestDraftDirectionOrder(CaseDetails caseDetails) {
        prepareFieldsForOrderApprovedCoverLetter(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        Optional<DraftDirectionOrder> draftDirectionOrderCollectionTail = hearingOrderService
            .draftDirectionOrderCollectionTail(caseDetails);

        draftDirectionOrderCollectionTail.ifPresentOrElse(
            directionOrderTail -> caseData.put(LATEST_DRAFT_DIRECTION_ORDER, directionOrderTail),
            () -> caseData.remove(LATEST_DRAFT_DIRECTION_ORDER)
        );

        return caseData;
    }

    public AboutToStartOrSubmitCallbackResponse handleUploadApprovedOrderAboutToSubmit(CaseDetails caseDetails,
                                                                                       String authorisationToken) {
        List<String> errors = new ArrayList<>();

        hearingOrderService.convertToPdfAndStampAndStoreLatestDraftHearingOrder(caseDetails, authorisationToken);
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);
        caseDataService.moveCollection(caseDetails.getData(), DRAFT_DIRECTION_DETAILS_COLLECTION, DRAFT_DIRECTION_DETAILS_COLLECTION_RO);

        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException | JsonProcessingException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData())
                .errors(errors).build();
        }

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        caseDetails.getData().remove(LATEST_DRAFT_DIRECTION_ORDER);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }

    private void prepareFieldsForOrderApprovedCoverLetter(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_TYPE);
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_NAME);
        caseData.remove(CONTESTED_ORDER_APPROVED_DATE);
    }
}
