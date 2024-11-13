package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.isJudgeReviewable;

@Slf4j
@Service
public class ApproveDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    private final HearingService hearingService;

    private static final String ERROR_MESSAGE = "There are no draft orders or pension sharing annexes to review.";

    public ApproveDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        List<String> errors = new ArrayList<>();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about to start callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        List<DynamicListElement> hearingOptions = new ArrayList<>();
        List<DraftOrdersReview> hearingsForReview = new ArrayList<>();

        //Check each hearing in draftOrdersReviewCollection
        if (!ObjectUtils.isEmpty(draftOrdersWrapper.getDraftOrdersReviewCollection())) {
            for (DraftOrdersReviewCollection reviewCollection : draftOrdersWrapper.getDraftOrdersReviewCollection()) {
                DraftOrdersReview draftOrdersReview = reviewCollection.getValue();

                boolean isReviewableHearing = draftOrdersReview.getDraftOrderDocReviewCollection().stream()
                    .anyMatch(doc -> isJudgeReviewable(doc.getValue().getOrderStatus()))
                    || draftOrdersReview.getPsaDocReviewCollection().stream()
                    .anyMatch(psa -> isJudgeReviewable(psa.getValue().getOrderStatus()));

                //Only add hearing if there is an order or PSA with TO_BE_REVIEWED
                if (isReviewableHearing) {
                    hearingsForReview.add(draftOrdersReview);
                }
            }
            if (ObjectUtils.isEmpty(hearingsForReview)) {
                errors.add(ERROR_MESSAGE);
            }
            
            //Sort the hearings by date
            hearingsForReview.sort(Comparator.comparing(DraftOrdersReview::getHearingDate));

            hearingOptions = hearingsForReview.stream()
                .map(draftOrdersReview -> DynamicListElement.builder()
                    .code(draftOrdersReview.getHearingId())
                    .label(
                        hearingService.formatHearingInfo(draftOrdersReview.getHearingType(),
                            draftOrdersReview.getHearingDate(),
                            draftOrdersReview.getHearingTime(),
                            draftOrdersReview.getHearingJudge()
                    ))
                    .build())
                .toList();
        } else {
            errors.add(ERROR_MESSAGE);
        }

        //Set the filtered hearings in the DynamicList
        DynamicList hearingsReadyForReview = DynamicList.builder()
            .value(null)
            .listItems(hearingOptions)
            .build();
        draftOrdersWrapper.setHearingsReadyForReview(hearingsReadyForReview);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).errors(errors).build();

    }
}
