package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnStartDefaultValueService {

    @Autowired
    private IdamService idamService;

    public void defaultCivilPartnershipField(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
    }

    public void defaultUrgencyQuestion(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(URGENT_CASE_QUESTION, NO_VALUE);
    }

    public void defaultTypeOfApplication(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(TYPE_OF_APPLICATION, TYPE_OF_APPLICATION_DEFAULT_TO);
    }

    public void defaultIssueDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(ISSUE_DATE, LocalDate.now());
    }

    public void defaultConsentedOrderJudgeName(CallbackRequest callbackRequest, String userAuthorisation) {
        callbackRequest.getCaseDetails().getData().put(CONSENTED_ORDER_DIRECTION_JUDGE_NAME,
            idamService.getIdamSurname(userAuthorisation));
    }

    public void defaultContestedOrderJudgeName(CallbackRequest callbackRequest, String userAuthorisation) {
        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_APPROVED_JUDGE_NAME,
            idamService.getIdamSurname(userAuthorisation));
    }

    public void defaultConsentedOrderDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CONSENTED_ORDER_DIRECTION_DATE, LocalDate.now());
    }

    public void defaultContestedOrderDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CONTESTED_ORDER_APPROVED_DATE, LocalDate.now());
    }


}
