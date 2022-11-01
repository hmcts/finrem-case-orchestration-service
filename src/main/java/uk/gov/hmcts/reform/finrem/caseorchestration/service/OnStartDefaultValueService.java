package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnStartDefaultValueService {

    private IdamService idamService;

    public void defaultIssueDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(ISSUE_DATE, LocalDate.now());
    }

    public void defaultJudgeName(CallbackRequest callbackRequest, String userAuthorisation) {
        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_JUDGE_NAME,
            idamService.getIdamFullName(userAuthorisation));
    }

    public void defaultOrderDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CONSENTED_ORDER_DIRECTION_DATE, LocalDate.now());
    }
}