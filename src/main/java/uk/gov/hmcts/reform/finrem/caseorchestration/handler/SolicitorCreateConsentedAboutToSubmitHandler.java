package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorCreateConsentedAboutToSubmitHandler implements CallbackHandler {

    private final ConsentOrderService consentOrderService;
    private final IdamService idamService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.SOLICITOR_CREATE.equals(eventType)
            || EventType.AMENDED_CONSENT_ORDER.equals(eventType)
            || EventType.RESPOND_TO_ORDER.equals(eventType));
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest,
                                                       String userAuthorisation) {
        log.info("Received request to update latest Consent Order with Case ID : {}", callbackRequest.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        Document caseDocument = consentOrderService.getLatestConsentOrderData(callbackRequest);
        caseData.setLatestConsentOrder(caseDocument);

        if (!idamService.isUserRoleAdmin(userAuthorisation)) {
            caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
    }
}
