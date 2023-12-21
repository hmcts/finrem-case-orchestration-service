package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

@Slf4j
@Service
public class ConsentApplicationApprovedInContestedAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService service;

    @Autowired
    public ConsentApplicationApprovedInContestedAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                                    IdamService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for {} Case ID: {}", EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED,
            caseDetails.getId());
        FinremCaseData data = caseDetails.getData();
        ConsentOrderWrapper consentOrderWrapper = data.getConsentOrderWrapper();
        if (consentOrderWrapper.getConsentJudgeName() == null) {
            consentOrderWrapper.setConsentJudgeName(service.getIdamFullName(userAuthorisation));
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(data).build();
    }
}
