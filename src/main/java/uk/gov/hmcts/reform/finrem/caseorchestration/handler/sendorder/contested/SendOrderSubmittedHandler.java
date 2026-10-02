package uk.gov.hmcts.reform.finrem.caseorchestration.handler.sendorder.contested;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderEventPostStateOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.sendorder.SendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SendOrderSubmittedHandler extends FinremSubmittedCallbackHandler {
    private final GeneralOrderService generalOrderService;
    private final CcdService ccdService;
    private final SendOrderCorresponder sendOrderCorresponder;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    public SendOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                     EvidenceManagementDeleteService evidenceManagementDeleteService,
                                     RetryExecutor retryExecutor,
                                     GeneralOrderService generalOrderService,
                                     CcdService ccdService,
                                     SendOrderCorresponder sendOrderCorresponder,
                                     CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.generalOrderService = generalOrderService;
        this.ccdService = ccdService;
        this.sendOrderCorresponder = sendOrderCorresponder;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
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
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<String> errors = sendNotifications(callbackRequest, generalOrderService.getParties(caseDetails), userAuthorisation);

        updateCaseWithPostStateOption(caseDetails, userAuthorisation);

        if (errors.isEmpty()) {
            return submittedResponse();
        } else {
            return submittedResponse(
                toConfirmationHeader("Send order event submitted with errors"),
                toConfirmationBody(errors.toArray(new String[0])));
        }
    }

    private void updateCaseWithPostStateOption(FinremCaseDetails caseDetails, String userAuthorisation) {
        SendOrderEventPostStateOption sendOrderPostStateOption = caseDetails.getData().getSendOrderWrapper().getSendOrderPostStateOption();
        if (isOptionThatRequireUpdate(sendOrderPostStateOption)) {
            caseDetails.getData().getSendOrderWrapper().setSendOrderPostStateOption(null);
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

    private List<String> sendNotifications(FinremCallbackRequest callbackRequest, List<String> parties, String userAuthorisation) {
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        // Set the party correspondence enabled flags. These changes will not be persisted.
        generalOrderService.setPartiesToReceiveCommunication(finremCaseDetails, parties);

        List<SendCorrespondenceEvent> events = sendOrderCorresponder.buildCorrespondenceEventIfNeeded(callbackRequest, userAuthorisation);
        final List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            if (!emptyIfNull(event.getNotificationParties()).isEmpty()) {
                String party = event.describeNotificationParties();
                String actionName = "Send order correspondence to party: %s for the send order event"
                    .formatted(party);
                correspondenceEventAuditOrchestrationService.publishEvent(event, actionName,
                    () -> errors.add(
                        "Unable to deliver send order correspondence to %s. Please send it manually.".formatted(party))
                );
            }
        }
        return errors;
    }
}
