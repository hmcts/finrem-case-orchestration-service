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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentSendOrderCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SendOrderConsentForNotApprovedOrderSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService;
    private final ConsentSendOrderCorresponder consentSendOrderCorresponder;

    public SendOrderConsentForNotApprovedOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                               EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                               RetryExecutor retryExecutor,
                                                               CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService,
                                                               ConsentSendOrderCorresponder consentSendOrderCorresponder) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.correspondenceEventAuditOrcService = correspondenceEventAuditOrcService;
        this.consentSendOrderCorresponder = consentSendOrderCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        List<String> errors = sendNotifications(callbackRequest, userAuthorisation);

        if (errors.isEmpty()) {
            return submittedResponse();
        }

        return submittedResponse(
            toConfirmationHeader("Send order event submitted with errors"),
            toConfirmationBody(errors.toArray(new String[0]))
        );
    }

    private List<String> sendNotifications(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        List<SendCorrespondenceEvent> events = consentSendOrderCorresponder
            .buildNotApprovedOrderEvents(caseDetails, userAuthorisation);
        final List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            if (!emptyIfNull(event.getNotificationParties()).isEmpty()) {
                String party = event.describeNotificationParties();
                String actionName = "Send consent order not approved correspondence to party: %s for the send order event"
                    .formatted(party);
                correspondenceEventAuditOrcService.publishEvent(event, actionName,
                    () -> errors.add(
                        "Unable to deliver consent order not approved correspondence to %s. Please send it manually."
                            .formatted(party))
                );
            }
        }
        return errors;
    }
}
