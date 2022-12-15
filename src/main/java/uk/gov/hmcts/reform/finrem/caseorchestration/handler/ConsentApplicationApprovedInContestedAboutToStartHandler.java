package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Objects;

@Slf4j
@Service
public class ConsentApplicationApprovedInContestedAboutToStartHandler extends FinremCallbackHandler {

    private final IdamService service;

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
        log.info("Received request for {} caseId {}", EventType.CONSENT_APPLICATION_APPROVED_IN_CONTESTED,
            callbackRequest.getCaseDetails().getId());

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        if (Objects.isNull(caseData.getConsentOrderWrapper().getConsentJudgeName())) {
            caseData.getConsentOrderWrapper().setConsentJudgeName(service.getIdamFullName(userAuthorisation));
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
