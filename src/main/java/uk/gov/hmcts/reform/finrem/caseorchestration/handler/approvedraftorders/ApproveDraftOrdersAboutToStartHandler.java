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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
public class ApproveDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    public ApproveDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
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
        String error = "There are no outstanding draft orders or pension sharing annexes that are ready to review.";
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about to start callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        List<DynamicListElement> hearingOptions = new ArrayList<>();
        List<DraftOrdersReview> hearingsForReview = new ArrayList<>();

        //Check each hearing in draftOrdersReviewCollection
        if (!draftOrdersWrapper.getDraftOrdersReviewCollection().isEmpty()) {
            for (DraftOrdersReviewCollection reviewCollection : draftOrdersWrapper.getDraftOrdersReviewCollection()) {
                DraftOrdersReview draftOrdersReview = reviewCollection.getValue();

                boolean isReviewableHearing = draftOrdersReview.getDraftOrderDocReviewCollection().stream()
                    .anyMatch(doc -> doc.getValue().getOrderStatus() == OrderStatus.TO_BE_REVIEWED)
                    || draftOrdersReview.getPsaDocReviewCollection().stream()
                    .anyMatch(psa -> psa.getValue().getOrderStatus() == OrderStatus.TO_BE_REVIEWED);

                //Only add hearing if there is an order or PSA with TO_BE_REVIEWED
                if (isReviewableHearing) {
                    hearingsForReview.add(draftOrdersReview);
                }
            }
            if (ObjectUtils.isEmpty(hearingsForReview)) {
                errors.add(error);
            }
            
            //Sort the hearings by date
            hearingsForReview.sort(Comparator.comparing(DraftOrdersReview::getHearingDate));

            hearingOptions = hearingsForReview.stream()
                .map(draftOrdersReview -> DynamicListElement.builder()
                    .code(draftOrdersReview.getHearingId())
                    .label(format(
                        "%s on %s %s by %s",
                        Optional.ofNullable(draftOrdersReview.getHearingType()).orElse("N/A"),
                        Optional.ofNullable(draftOrdersReview.getHearingDate())
                            .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .orElse("N/A"),
                        Optional.ofNullable(draftOrdersReview.getHearingTime()).orElse("N/A"),
                        Optional.ofNullable(draftOrdersReview.getHearingJudge()).orElse("N/A")
                    ))
                    .build())
                .toList();
        } else {
            errors.add(error);
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
