package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultValueAboutToStartHandler implements CallbackHandler {

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_CASE.equals(eventType)
            || EventType.AMEND_APPLICATION.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        if (caseData.getCivilPartnership() == null) {
            caseData.setCivilPartnership(YesOrNo.NO);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
