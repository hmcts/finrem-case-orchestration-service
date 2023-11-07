package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremContestedSendOrderCorresponder;

import java.util.List;

@Slf4j
@Service
public class SendOrderContestedSubmittedHandler extends FinremCallbackHandler {
    private final GeneralOrderService generalOrderService;
    private final CcdService ccdService;
    private final FinremContestedSendOrderCorresponder contestedSendOrderCorresponder;


    public SendOrderContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                              GeneralOrderService generalOrderService,
                                              CcdService ccdService,
                                              FinremContestedSendOrderCorresponder contestedSendOrderCorresponder) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.ccdService = ccdService;
        this.contestedSendOrderCorresponder = contestedSendOrderCorresponder;
    }


    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} submitted callback for case id: {}", callbackRequest.getEventType(), caseDetails.getId());

        List<String> parties = generalOrderService.getParties(caseDetails);
        log.info("Selected parties {} on case {}", parties, caseDetails.getId());

        sendNotifications(callbackRequest, parties, userAuthorisation);

        updateCaseWithPostStateOption(caseDetails, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void updateCaseWithPostStateOption(FinremCaseDetails<FinremCaseDataContested> caseDetails,
                                               String userAuthorisation) {

        SendOrderEventPostStateOption sendOrderPostStateOption = caseDetails.getData().getSendOrderPostStateOption();
        if (isOptionThatRequireUpdate(sendOrderPostStateOption)) {
            caseDetails.getData().setSendOrderPostStateOption(null);
            ccdService.executeCcdEventOnCase(
                userAuthorisation,
                String.valueOf(caseDetails.getId()),
                caseDetails.getCaseType().getCcdType(),
                sendOrderPostStateOption.getEventToTrigger().getCcdType());
        }
    }

    private boolean isOptionThatRequireUpdate(SendOrderEventPostStateOption postStateOption) {
        return postStateOption.getEventToTrigger().equals(EventType.PREPARE_FOR_HEARING)
            || postStateOption.getEventToTrigger().equals(EventType.CLOSE);
    }


    private void sendNotifications(FinremCallbackRequest callbackRequest, List<String> parties, String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        generalOrderService.setPartiesToReceiveCommunication(caseDetails, parties);
        log.info("About to start send order correspondence for case {}", caseDetails.getId());
        contestedSendOrderCorresponder.sendCorrespondence(caseDetails, userAuthorisation);
        log.info("Finish sending order correspondence for case {}", caseDetails.getId());
    }

}
