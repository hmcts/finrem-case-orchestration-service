package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SendOrderConsentForApprovedOrderSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ConsentOrderApprovedCorresponder consentOrderApprovedCorresponder;
    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    public SendOrderConsentForApprovedOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                            RetryExecutor retryExecutor,
                                                            ConsentOrderApprovedCorresponder consentOrderApprovedCorresponder,
                                                            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.consentOrderApprovedCorresponder = consentOrderApprovedCorresponder;
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrcService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        List<String> errors = sendCorrespondence(callbackRequest, userAuthorisation);

        if (errors.isEmpty()) {
            return submittedResponse();
        }
        return submittedResponse(
            toConfirmationHeader("Send order event submitted with errors"),
            toConfirmationBody(errors.toArray(new String[0])));
    }

    private List<String> sendCorrespondence(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        List<SendCorrespondenceEvent> events = consentOrderApprovedCorresponder
            .buildCorrespondenceEvents(callbackRequest.getCaseDetails(), userAuthorisation);

        List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            String party = event.describeNotificationParties();
            String actionName = "Send consent order correspondence to party: %s for the send order event".formatted(party);
            boolean sent = correspondenceEventAuditOrchestrationService.publishEvent(event, actionName);
            if (!sent) {
                errors.add("Unable to deliver send order correspondence to %s. Please send it manually.".formatted(party));
            }
        }
        return errors;
    }
}
