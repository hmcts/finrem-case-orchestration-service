package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.AnotherHearingRequestCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ApproveDraftOrdersMidEventHandler extends FinremCallbackHandler {

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

        boolean isHearingInstructionRequired = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> locateJudgeApproval(draftOrdersWrapper, i))
            .filter(Objects::nonNull)
            .map(JudgeApproval::getJudgeDecision)
            .anyMatch(decision -> decision != null && decision.isHearingInstructionRequired());

        draftOrdersWrapper.getHearingInstruction().setShowRequireAnotherHearingQuestion(YesOrNo.forValue(isHearingInstructionRequired));

        draftOrdersWrapper.getHearingInstruction().setAnotherHearingRequestCollection(List.of(
            AnotherHearingRequestCollection.builder()
                .value(AnotherHearingRequest.builder()
                    .whichOrder(approveOrderService.buildWhichOrderDynamicList(draftOrdersWrapper))
                    .build())
                .build()
        ));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

    private JudgeApproval locateJudgeApproval(DraftOrdersWrapper draftOrdersWrapper, int index) {
        return switch (index) {
            case 1 -> draftOrdersWrapper.getJudgeApproval1();
            case 2 -> draftOrdersWrapper.getJudgeApproval2();
            case 3 -> draftOrdersWrapper.getJudgeApproval3();
            case 4 -> draftOrdersWrapper.getJudgeApproval4();
            case 5 -> draftOrdersWrapper.getJudgeApproval5();
            default -> null;
        };
    }

}
