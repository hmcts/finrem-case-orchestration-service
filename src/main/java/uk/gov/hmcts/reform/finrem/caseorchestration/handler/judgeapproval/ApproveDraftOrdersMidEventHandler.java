package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.HearingInstruction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApprovalDocType.PSA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.JUDGE_NEEDS_TO_MAKE_CHANGES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeDecision.READY_TO_BE_SEALED;

@Slf4j
@Service
public class ApproveDraftOrdersMidEventHandler extends FinremCallbackHandler {

    private static final String SEPARATOR = "#";

    private final ApproveOrderService approveOrderService;

    public ApproveDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, ApproveOrderService approveOrderService) {
        super(finremCaseDetailsMapper);
        this.approveOrderService = approveOrderService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        setupHearingInstruction(draftOrdersWrapper);
        setupExtraReportFieldsInput(draftOrdersWrapper);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

    private void setupHearingInstruction(DraftOrdersWrapper draftOrdersWrapper) {
        boolean isHearingInstructionRequired = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> approveOrderService.resolveJudgeApproval(draftOrdersWrapper, i))
            .filter(Objects::nonNull)
            .map(JudgeApproval::getJudgeDecision)
            .anyMatch(decision -> decision != null && decision.isHearingInstructionRequired());

        draftOrdersWrapper.setHearingInstruction(HearingInstruction.builder()
            .showRequireAnotherHearingQuestion(YesOrNo.forValue(isHearingInstructionRequired))
            .anotherHearingRequestCollection(List.of(
                AnotherHearingRequestCollection.builder()
                    .value(AnotherHearingRequest.builder()
                        .whichOrder(buildWhichOrderDynamicList(draftOrdersWrapper))
                        .build())
                    .build()
            ))
            .build());
    }

    private void setupExtraReportFieldsInput(DraftOrdersWrapper draftOrdersWrapper) {
        boolean isExtraReportFieldsInputRequired = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> approveOrderService.resolveJudgeApproval(draftOrdersWrapper, i))
            .filter(Objects::nonNull)
            .map(JudgeApproval::getJudgeDecision)
            .anyMatch(decision -> decision != null && decision.isExtraReportFieldsInputRequired());

        draftOrdersWrapper.setExtraReportFieldsInput(ExtraReportFieldsInput.builder()
            .showRequireExtraReportFieldsInputQuestion(YesOrNo.forValue(isExtraReportFieldsInputRequired))
            .build());
    }

    /**
     * Builds a {@link DynamicList} representing the orders available for selection
     * based on the given {@link DraftOrdersWrapper}. Each order is represented as a
     * {@link DynamicListElement} containing a code and label.
     *
     * <p>The method processes up to 5 judge approvals, where each approval contributes a list item
     * if the approval is non-null and the associated document filename is not empty.</p>
     *
     * <p>For each {@link JudgeApproval}:</p>
     * <ul>
     *     <li>The code is prefixed with "draftOrder" or "psa", depending on the document type.</li>
     *     <li>The filename is used as the label of the list item.</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>
     * Code: draftOrder_1, Label: OrderDocument1.pdf
     * Code: psa_2, Label: PensionSharingAnnex1.pdf
     * </pre>
     *
     * @param draftOrdersWrapper the {@link DraftOrdersWrapper} containing the judge approvals.
     * @return a {@link DynamicList} populated with items representing the orders.
     */
    private DynamicList buildWhichOrderDynamicList(DraftOrdersWrapper draftOrdersWrapper) {
        List<DynamicListElement> listItems = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            JudgeApproval judgeApproval = approveOrderService.resolveJudgeApproval(draftOrdersWrapper, i);
            if (judgeApproval != null) {
                String codePrefix = DRAFT_ORDER == judgeApproval.getDocType() ? DRAFT_ORDER.name() : PSA.name();
                String code = codePrefix + SEPARATOR + i;

                String filename = getDocumentFileName(judgeApproval);
                if (!StringUtils.isEmpty(filename)) {
                    listItems.add(DynamicListElement.builder().code(code).label(filename).build());
                }
            }
        }

        return DynamicList.builder().listItems(listItems).build();
    }

    private String getDocumentFileName(JudgeApproval judgeApproval) {
        String filename = null;
        if (JUDGE_NEEDS_TO_MAKE_CHANGES == judgeApproval.getJudgeDecision()) {
            if (judgeApproval.getAmendedDocument() == null) {
                throw new IllegalStateException("Expected amended document was not found.");
            }
            filename = judgeApproval.getAmendedDocument().getDocumentFilename();
        } else if (READY_TO_BE_SEALED == judgeApproval.getJudgeDecision()) {
            if (judgeApproval.getDocument() == null) {
                throw new IllegalStateException("Expected document was not found.");
            }
            filename = judgeApproval.getDocument().getDocumentFilename();
        }
        return filename;
    }
}
