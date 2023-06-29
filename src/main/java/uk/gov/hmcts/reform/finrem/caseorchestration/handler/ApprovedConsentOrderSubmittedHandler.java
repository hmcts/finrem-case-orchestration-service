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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderMadeCorresponder;

@Slf4j
@Service
public class ApprovedConsentOrderSubmittedHandler extends FinremCallbackHandler {

    private final FinremConsentOrderMadeCorresponder consentOrderMadeCorresponder;
    private final FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;

    @Autowired
    public ApprovedConsentOrderSubmittedHandler(FinremConsentOrderMadeCorresponder consentOrderMadeCorresponder,
                                                FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder,
                                                FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
        this.consentOrderMadeCorresponder = consentOrderMadeCorresponder;
        this.consentOrderAvailableCorresponder = consentOrderAvailableCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        consentOrderMadeCorresponder.sendCorrespondence(caseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseData>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

}
