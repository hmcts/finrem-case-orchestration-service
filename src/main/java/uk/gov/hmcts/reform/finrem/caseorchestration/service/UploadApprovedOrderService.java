package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CollectionElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadApprovedOrderService {

    private final HearingOrderService hearingOrderService;
    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final GenericDocumentService genericDocumentService;
    private final ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    public Map<String, Object> prepareFieldsForOrderApprovedCoverLetter(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_TYPE);
        caseData.remove(CONTESTED_ORDER_APPROVED_JUDGE_NAME);
        caseData.remove(CONTESTED_ORDER_APPROVED_DATE);
        caseData.remove(HEARING_NOTICE_DOCUMENT_PACK);

        return caseData;
    }

    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handleUploadApprovedOrderAboutToSubmit(CaseDetails caseDetails,
                                                                                                                   String authorisationToken) {
        List<String> errors = new ArrayList<>();
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, authorisationToken);

        try {
            additionalHearingDocumentService.createAndStoreAdditionalHearingDocumentsFromApprovedOrder(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).errors(errors).build();
        }

        hearingOrderService.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);
        if (isAnotherHearingToBeListed(caseDetails)) {
            approvedOrderNoticeOfHearingService.createAndStoreHearingNoticeDocumentPack(caseDetails, authorisationToken);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private boolean isAnotherHearingToBeListed(CaseDetails caseDetails) {
        Optional<AdditionalHearingDirectionsCollection> latestHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);
        return latestHearingDirections.isPresent()
            && YES_VALUE.equals(latestHearingDirections.get().getIsAnotherHearingYN());
    }

    private Optional<AdditionalHearingDirectionsCollection> getLatestAdditionalHearingDirections(CaseDetails caseDetails) {
        List<Element<AdditionalHearingDirectionsCollection>> additionalHearingDetailsCollection =
            new ObjectMapper().convertValue(caseDetails.getData().get(HEARING_DIRECTION_DETAILS_COLLECTION),
                new TypeReference<>() {
                });

        return additionalHearingDetailsCollection != null && !additionalHearingDetailsCollection.isEmpty()
            ? Optional.of(additionalHearingDetailsCollection.get(additionalHearingDetailsCollection.size() - 1).getValue())
            : Optional.empty();
    }

    private Optional<CollectionElement<DirectionOrder>> getLatestHearingOrder(List<CollectionElement<DirectionOrder>> directionOrders) {
        return directionOrders.isEmpty()
            ? Optional.empty()
            : Optional.of(directionOrders.get(directionOrders.size() - 1));
    }

    private List<CollectionElement<DirectionOrder>> getHearingOrderList(Map<String, Object> caseData) {
        return Optional.ofNullable(caseData.get(HEARING_ORDER_COLLECTION))
            .map(this::convertToListOfHearingOrder)
            .orElse(new ArrayList<>());
    }

    private List<CollectionElement<DirectionOrder>> convertToListOfHearingOrder(Object value) {
        return new ObjectMapper().convertValue(value, new TypeReference<>() {
        });
    }
}
