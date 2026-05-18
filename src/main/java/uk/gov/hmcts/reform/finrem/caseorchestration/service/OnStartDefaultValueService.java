package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_ORDER_DIRECTION_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

@Service
@RequiredArgsConstructor
public class OnStartDefaultValueService {

    private final IdamService idamService;

    public void defaultCivilPartnershipField(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CIVIL_PARTNERSHIP, NO_VALUE);
    }

    public void defaultCivilPartnershipField(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getCivilPartnership() == null) {
            caseData.setCivilPartnership(YesOrNo.NO);
        }
    }

    public void defaultUrgencyQuestion(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(URGENT_CASE_QUESTION, NO_VALUE);
    }

    public void defaultUrgencyQuestion(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getPromptForUrgentCaseQuestion() == null) {
            caseData.setPromptForUrgentCaseQuestion(YesOrNo.NO);
        }
    }

    public void defaultTypeOfApplication(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(TYPE_OF_APPLICATION, TYPE_OF_APPLICATION_DEFAULT_TO);
    }

    public void defaultTypeOfApplication(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (caseData.getScheduleOneWrapper().getTypeOfApplication() == null) {
            caseData.getScheduleOneWrapper().setTypeOfApplication(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS);
        }
    }

    public void defaultIssueDate(FinremCallbackRequest callbackRequest) {
        FinremCaseData data = callbackRequest.getCaseDetails().getData();
        if (data.getIssueDate() == null) {
            data.setIssueDate(LocalDate.now());
        }
    }

    /**
     * Sets the consented order direction judge name using the surname
     * retrieved from the authenticated IDAM user.
     *
     * @param finremCaseData the case data to update with the judge name
     * @param userAuthorisation the user authorisation token used to retrieve the IDAM user details
     */
    public void defaultConsentedOrderJudgeName(FinremCaseData finremCaseData, String userAuthorisation) {
        finremCaseData.setOrderDirectionJudgeName(idamService.getIdamSurname(userAuthorisation));
    }

    public void defaultContestedOrderJudgeName(CallbackRequest callbackRequest, String userAuthorisation) {
        callbackRequest.getCaseDetails().getData().put(CONTESTED_ORDER_APPROVED_JUDGE_NAME,
            idamService.getIdamSurname(userAuthorisation));
    }

    /**
     * Sets the consented order direction date to the current system date.
     *
     * @param finremCaseData the case data to update with the current order direction date
     */
    public void defaultConsentedOrderDate(FinremCaseData finremCaseData) {
        finremCaseData.setOrderDirectionDate(LocalDate.now());
    }

    public void defaultContestedOrderDate(CallbackRequest callbackRequest) {
        callbackRequest.getCaseDetails().getData().putIfAbsent(CONTESTED_ORDER_APPROVED_DATE, LocalDate.now());
    }
}
