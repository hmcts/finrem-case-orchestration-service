package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmendApplicationConsentedMidHandler implements CallbackHandler<Map<String, Object>> {
    private final ConsentOrderService consentOrderService;
    private final InternationalPostalService postalService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.AMEND_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        log.info("Invoking amend application mid event for caseId {}", callbackRequest.getCaseDetails().getId());
        List<String> errors = consentOrderService.performCheck(callbackRequest, userAuthorisation);
        List<String> validate = postalService.validate(callbackRequest.getCaseDetails().getData());
        errors.addAll(validate);

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder()
            .data(callbackRequest.getCaseDetails().getData()).errors(errors).build();
    }
}
