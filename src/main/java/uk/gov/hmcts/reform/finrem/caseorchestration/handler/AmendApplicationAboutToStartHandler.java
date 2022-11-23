package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Intention;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AmendApplicationAboutToStartHandler extends FinremCallbackHandler {

    public AmendApplicationAboutToStartHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.AMEND_APP_DETAILS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Handling amend application about to start callback for case id: {}", callbackRequest.getCaseDetails().getId());
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to set nature of application for consented case with Case ID: {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        log.info("caseData ={}= for case id {}", caseData, caseDetails.getId());
        final Intention intention = caseData.getApplicantIntendsTo();
        log.info("Applicant intends to {} for case id: {}", intention, caseDetails.getId());

        if (Intention.APPLY_TO_VARY.equals(intention)) {
            log.info("Add applicant intends to {} to nature of application", intention.getValue());
            List<NatureApplication> natureApplicationList =
                Optional.ofNullable(caseData.getNatureApplicationWrapper().getNatureOfApplication2()).orElse(new ArrayList<>());
            natureApplicationList.add(NatureApplication.VARIATION_ORDER);
            caseData.getNatureApplicationWrapper().setNatureOfApplication2(natureApplicationList);
            log.info("paper case {} marked as variation order", caseDetails.getId());
        }

        if (caseData.getCivilPartnership() == null) {
            caseData.setCivilPartnership(YesOrNo.NO);
        }

        if (caseData.getPromptForUrgentCaseQuestion() == null) {
            caseData.setPromptForUrgentCaseQuestion(YesOrNo.NO);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }


}
