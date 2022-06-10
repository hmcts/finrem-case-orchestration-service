package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DRAFT_DIRECTION_DETAILS_COLLECTION_RO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DIRECTION_ORDER_IS_FINAL;
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

    public Map<String, Object> setIsFinalHearingFieldMidEvent(CaseDetails caseDetails) {
        Optional<DraftDirectionDetails> draftDirectionDetailsOptional =
            getDraftDirectionDetailsCollectionTail(caseDetails);

        draftDirectionDetailsOptional.ifPresentOrElse(
            latestDraftDirections -> caseDetails.getData().put(LATEST_DIRECTION_ORDER_IS_FINAL, latestDraftDirections.getIsFinal()),
            () -> caseDetails.getData().put(LATEST_DIRECTION_ORDER_IS_FINAL, NO_VALUE)
        );


        return caseDetails.getData();
    }

    public AboutToStartOrSubmitCallbackResponse handleUploadApprovedOrderAboutToSubmit(CaseDetails caseDetails,
                                                                                       String authorisationToken) {
        List<String> errors = new ArrayList<>();

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

    private Optional<DraftDirectionDetails> getDraftDirectionDetailsCollectionTail(CaseDetails caseDetails) {
        List<Element<DraftDirectionDetails>> draftDirectionDetailsCollection = Optional.ofNullable(caseDetails.getData()
            .get(DRAFT_DIRECTION_DETAILS_COLLECTION))
            .map(this::convertToDraftDirectionDetails)
            .orElse(Collections.emptyList());

        return draftDirectionDetailsCollection.isEmpty()
            ? Optional.empty()
            : Optional.of(draftDirectionDetailsCollection.get(draftDirectionDetailsCollection.size() - 1).getValue());
    }

    private List<Element<DraftDirectionDetails>> convertToDraftDirectionDetails(Object value) {
        return new ObjectMapper().convertValue(value, new TypeReference<>() {});
    }
}
