package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedAboutToStartHandler implements CallbackHandler {

    private final OnStartDefaultValueService service;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        service.defaultCivilPartnershipField(callbackRequest);
        return AboutToStartOrSubmitCallbackResponse.builder().data(callbackRequest.getCaseDetails().getData()).build();
    }
}
