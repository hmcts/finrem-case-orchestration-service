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
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Intention;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationAboutToStartHandler implements CallbackHandler {

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_APPLICATION_DETAILS.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to set nature of application for consented case with Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getCaseData();
        log.info("caseData ={}= for case id {}", caseData, caseDetails.getId());
        final Intention intention = caseData.getApplicantIntendsTo();
        log.info("Applicant intends to {} for case id: {}", intention, caseDetails.getId());

        if (Intention.APPLY_TO_VARY.equals(intention))  {
            log.info("Add applicant intends to {} to nature of application", intention.getValue());
            List<NatureApplication> natureApplicationList =
                Optional.ofNullable(caseData.getNatureApplicationWrapper().getNatureOfApplication2()).orElse(new ArrayList<>());
            natureApplicationList.add(NatureApplication.VARIATION_ORDER);
            caseData.getNatureApplicationWrapper().setNatureOfApplication2(natureApplicationList);
            log.info("paper case {} marked as variation order",caseDetails.getId());
        }

        if (caseData.getCivilPartnership() == null) {
            caseData.setCivilPartnership(YesOrNo.NO);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
