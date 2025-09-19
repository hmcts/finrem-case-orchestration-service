package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.List;
import java.util.function.Predicate;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.JUDGE_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class JudgeDraftOrderAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService idamService;

    public JudgeDraftOrderAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              IdamService idamService) {
        super(finremCaseDetailsMapper);
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType) && CONTESTED.equals(caseType) && JUDGE_DRAFT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        prepareJudgeApprovedOrderCollection(finremCaseData);
        prepareFieldsForOrderApprovedCoverLetter(finremCaseData, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseDetails.getData()).build();
    }

    private void prepareJudgeApprovedOrderCollection(FinremCaseData finremCaseData) {
        List<DraftDirectionOrderCollection> judgeApprovedOrderCollection = emptyIfNull(
            finremCaseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection()
        ).stream()
            .filter(isNotLegacyDraftOrder())
            .toList();
        finremCaseData.getDraftDirectionWrapper().setJudgeApprovedOrderCollection(judgeApprovedOrderCollection);
    }

    private Predicate<DraftDirectionOrderCollection> isNotLegacyDraftOrder() {
        return d -> d.getValue().getPurposeOfDocument() == null;
    }

    private void prepareFieldsForOrderApprovedCoverLetter(FinremCaseData finremCaseData, String authorisationToken) {
        finremCaseData.setOrderApprovedJudgeType(null);
        finremCaseData.setOrderApprovedJudgeName(idamService.getIdamSurname(authorisationToken));
        finremCaseData.setOrderApprovedDate(null);
    }
}
