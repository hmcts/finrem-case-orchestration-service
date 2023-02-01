package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.ConsentOrderMadeCorresponder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovedConsentOrderSubmittedHandler implements CallbackHandler<Map<String, Object>> {

    private final ConsentOrderMadeCorresponder consentOrderMadeCorresponder;
    private final ConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

}
