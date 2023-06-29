package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final ConsentOrderService consentOrderService;
    private final IdamService idamService;
    private final CaseFlagsService caseFlagsService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMEND_CONSENT_ORDER.equals(eventType)
            || EventType.RESPOND_TO_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        log.info("Received request to update latest Consent Order with Case ID : {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        CaseDocument caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.put(LATEST_CONSENT_ORDER, caseDocument);
        caseFlagsService.setCaseFlagInformation(callbackRequest.getCaseDetails());

        if (!idamService.isUserRoleAdmin(userAuthorisation)) {
            caseData.put(APPLICANT_REPRESENTED, YES_VALUE);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseData).build();
    }
}
