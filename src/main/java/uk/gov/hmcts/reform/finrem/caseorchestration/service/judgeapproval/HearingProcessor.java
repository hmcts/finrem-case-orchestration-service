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

/**
 * The HearingProcessor class handles the processing of hearing instructions
 * for draft orders, including validation of input data and updating the draft orders
 * based on the provided hearing request.
 */
@Component
class HearingProcessor {

    private static final String SEPARATOR = "#";

    /**
     * Processes the hearing instruction related to a draft order based on the
     * provided DraftOrdersWrapper and AnotherHearingRequest. This method
     * handles the extraction and validation of the selected value, invokes the
     * appropriate method for retrieving judge approval, and processes review
     * collections.
     *
     * @param draftOrdersWrapper the wrapper object containing the draft orders
     * @param anotherHearingRequest the request containing hearing-related data
     * @throws IllegalStateException if there is a missing or unexpected value
     *         in the AnotherHearingRequest or if the reflection-based method
     *         invocation fails
     */
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
        // Process hearing instructions based on the judge approval and draft order review collections
        ofNullable(judgeApproval)
            .map(JudgeApproval::getDocument).ifPresent(targetDoc -> ofNullable(draftOrdersWrapper.getDraftOrdersReviewCollection())
                .ifPresent(collection -> collection.forEach(el -> {
                    if (el.getValue() != null) {
                        // Process draft order document review collection
                        ofNullable(el.getValue().getDraftOrderDocReviewCollection())
                            .ifPresent(draftOrderDocReviewCollection ->
                                processHearingInstruction(draftOrderDocReviewCollection.stream()
                                    .map(DraftOrderDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );

                        // Process PSA document review collection
                        ofNullable(el.getValue().getPsaDocReviewCollection())
                            .ifPresent(psaDocReviewCollection ->
                                processHearingInstruction(psaDocReviewCollection.stream()
                                    .map(PsaDocReviewCollection::getValue).toList(), targetDoc, anotherHearingRequest)
                            );
                    }
                })));
    }

    /**
     * Processes a list of hearing instructions and updates their properties based on
     * the target document and hearing request. This method modifies the
     * properties of each instruction to reflect the hearing details (e.g.
     * hearing type, additional time, time estimate).
     *
     * @param hip the list of hearing instructions to process
     * @param targetDoc the document representing the target case
     * @param anotherHearingRequest the request containing additional hearing details
     */
    void processHearingInstruction(List<? extends HearingInstructionProcessable> hip,
                                   CaseDocument targetDoc,
                                   AnotherHearingRequest anotherHearingRequest) {
        ofNullable(hip)
            .ifPresent(list -> list.forEach(el -> {
                // Check if the hearing instruction matches the target document
                if (el.match(targetDoc)) {
                    // Update hearing instruction properties based on the hearing request
                    el.setAnotherHearingToBeListed(YesOrNo.YES);
                    el.setHearingType(anotherHearingRequest.getTypeOfHearing().name());
                    el.setAdditionalTime(anotherHearingRequest.getAdditionalTime());
                    el.setHearingTimeEstimate(anotherHearingRequest.getTimeEstimate().getValue());
                    el.setOtherListingInstructions(anotherHearingRequest.getAnyOtherListingInstructions());
                }
            }));
    }
}
