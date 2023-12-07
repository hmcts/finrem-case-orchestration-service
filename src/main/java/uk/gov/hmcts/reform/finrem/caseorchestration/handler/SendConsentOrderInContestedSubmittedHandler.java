package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentInContestedSendOrderCorresponder;

import java.util.List;

@Slf4j
@Service
public class SendConsentOrderInContestedSubmittedHandler extends FinremCallbackHandler {
    private final GeneralOrderService generalOrderService;
    private final FinremConsentInContestedSendOrderCorresponder contestedSendOrderCorresponder;

    public SendConsentOrderInContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       GeneralOrderService generalOrderService,
                                                       FinremConsentInContestedSendOrderCorresponder contestedSendOrderCorresponder) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.contestedSendOrderCorresponder = contestedSendOrderCorresponder;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_CONSENT_IN_CONTESTED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} submitted callback for Case ID: {}", callbackRequest.getEventType(), caseDetails.getId());

        List<String> parties = generalOrderService.getParties(caseDetails);
        log.info("Selected parties {} on Case ID: {}", parties, caseDetails.getId());

        sendNotifications(callbackRequest, parties, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void sendNotifications(FinremCallbackRequest callbackRequest, List<String> parties, String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        generalOrderService.setPartiesToReceiveCommunication(caseDetails, parties);
        log.info("About to start send order correspondence for Case ID: {}", caseDetails.getId());
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails, userAuthorisation);
        log.info("Finish sending order correspondence for Case ID: {}", caseDetails.getId());
    }
}
