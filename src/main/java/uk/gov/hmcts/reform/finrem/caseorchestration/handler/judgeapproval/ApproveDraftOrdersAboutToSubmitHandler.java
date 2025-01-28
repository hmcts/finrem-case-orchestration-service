package uk.gov.hmcts.reform.finrem.caseorchestration.handler.judgeapproval;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.JudgeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ExtraReportFieldsInput;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedOrderApprovedLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.judgeapproval.ApproveOrderService;

import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class ApproveDraftOrdersAboutToSubmitHandler extends FinremCallbackHandler {

    private final ApproveOrderService approveOrderService;

    private final ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;

    private final IdamService idamService;

    public ApproveDraftOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, ApproveOrderService approveOrderService,
                                                  ContestedOrderApprovedLetterService contestedOrderApprovedLetterService,
                                                  IdamService idamService) {
        super(finremCaseDetailsMapper);
        this.approveOrderService = approveOrderService;
        this.contestedOrderApprovedLetterService = contestedOrderApprovedLetterService;
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType) && CaseType.CONTESTED.equals(caseType) && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about-to-submit event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        Pair<Boolean, Boolean> statuses = approveOrderService.populateJudgeDecisions(caseDetails, draftOrdersWrapper, userAuthorisation);
        if (containsApprovalStatus(statuses)) {
            generateAndStoreCoverLetter(caseDetails, userAuthorisation);
        }
        clearInputFields(draftOrdersWrapper);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }

    private String readJudgeType(FinremCaseDetails finremCaseDetails) {
        String judgeType = ofNullable(finremCaseDetails.getData().getDraftOrdersWrapper().getExtraReportFieldsInput())
            .map(ExtraReportFieldsInput::getJudgeType)
            .map(JudgeType::getValue)
            .orElse("");
        if (judgeType.isEmpty()) {
            log.warn("{} - Judge type was not captured and an empty string will be shown in the cover letter.", finremCaseDetails.getId());
        }
        return judgeType;
    }

    private void generateAndStoreCoverLetter(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(finremCaseDetails,
            buildJudgeDetails(readJudgeType(finremCaseDetails), idamService.getIdamFullName(userAuthorisation)), userAuthorisation);
    }

    private String buildJudgeDetails(String judgeType, String judgeName) {
        return StringUtils.join(Stream.of(judgeType, judgeName).filter(StringUtils::isNotBlank).toArray(String[]::new), " ");
    }

    private boolean containsApprovalStatus(Pair<Boolean, Boolean> statuses) {
        return Boolean.TRUE.equals(statuses.getLeft());
    }

    private void clearInputFields(DraftOrdersWrapper draftOrdersWrapper) {
        draftOrdersWrapper.setJudgeApproval1(null);
        draftOrdersWrapper.setJudgeApproval2(null);
        draftOrdersWrapper.setJudgeApproval3(null);
        draftOrdersWrapper.setJudgeApproval4(null);
        draftOrdersWrapper.setJudgeApproval5(null);
        draftOrdersWrapper.setHearingInstruction(null);
        draftOrdersWrapper.setShowWarningMessageToJudge(null);
    }
}
