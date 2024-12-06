package uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingInstructionProcessable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Component
class HearingProcessor {

    private static final String SEPARATOR = "#";

    void processHearingInstruction(DraftOrdersWrapper draftOrdersWrapper, AnotherHearingRequest anotherHearingRequest) {
        String[] splitResult = ofNullable(anotherHearingRequest)
            .map(AnotherHearingRequest::getWhichOrder)
            .map(DynamicList::getValueCode)
            .map(valueCode -> valueCode.split(SEPARATOR))
            .orElseThrow(() -> new IllegalStateException("Missing selected value in AnotherHearingRequest.whichOrder"));
        if (splitResult.length != 2) {
            String valueCode = Optional.of(anotherHearingRequest)
                .map(AnotherHearingRequest::getWhichOrder)
                .map(DynamicList::getValueCode)
                .orElse(null);
            throw new IllegalStateException(format("Unexpected selected value in AnotherHearingRequest.whichOrder: %s", valueCode));
        }

        String orderIndex = splitResult[1];

        JudgeApproval judgeApproval = null;
        try {
            judgeApproval = (JudgeApproval) draftOrdersWrapper.getClass().getMethod("getJudgeApproval" + (orderIndex))
                .invoke(draftOrdersWrapper);
        } catch (Exception e) {
            throw new IllegalStateException(format("Unexpected method \"getJudgeApproval%s\" was invoked", orderIndex), e);
        }
        ofNullable(judgeApproval)
            .map(JudgeApproval::getDocument).ifPresent(targetDoc -> ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
                .ifPresent(collection -> collection.forEach(el -> {
                    if (el.getValue() != null) {
                        ofNullable(el.getValue().getDraftOrderDocReviewCollection())
                            .ifPresent(draftOrderDocReviewCollection ->
                                processHearingInstruction(draftOrderDocReviewCollection.stream()
                                    .map(DraftOrderDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );

                        ofNullable(el.getValue().getPsaDocReviewCollection())
                            .ifPresent(psaDocReviewCollection ->
                                processHearingInstruction(psaDocReviewCollection.stream()
                                    .map(PsaDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );
                    }
                })));
    }

    void processHearingInstruction(List<? extends HearingInstructionProcessable> hip,
                                   CaseDocument targetDoc,
                                   AnotherHearingRequest anotherHearingRequest) {
        ofNullable(hip)
            .ifPresent(list -> list.forEach(el -> {
                if (el.match(targetDoc)) {
                    el.setAnotherHearingToBeListed(YesOrNo.YES);
                    el.setHearingType(anotherHearingRequest.getTypeOfHearing().name());
                    el.setAdditionalTime(anotherHearingRequest.getAdditionalTime());
                    el.setHearingTimeEstimate(anotherHearingRequest.getTimeEstimate().getValue());
                    el.setOtherListingInstructions(anotherHearingRequest.getAnyOtherListingInstructions());
                }
            }));
    }
}
