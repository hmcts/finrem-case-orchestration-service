package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.generalorder.FinremGeneralOrderRaisedCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;

@Service
@Slf4j
public class CreateGeneralOrderContestedSubmittedHandler extends FinremCallbackHandler {

    private final FinremGeneralOrderRaisedCorresponder generalOrderRaisedCorresponder;

    protected CreateGeneralOrderContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       FinremGeneralOrderRaisedCorresponder generalOrderRaisedCorresponder) {
        super(finremCaseDetailsMapper);
        this.generalOrderRaisedCorresponder = generalOrderRaisedCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType) && CaseType.CONTESTED.equals(caseType)
            && (GENERAL_ORDER.equals(eventType) || GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info("Contested Create General Order submitted callback for case id: {}",
            callbackRequest.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        generalOrderRaisedCorresponder.sendCorrespondence(caseDetails, callbackRequest.getEventType());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
