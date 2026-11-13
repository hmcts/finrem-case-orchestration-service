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

@Slf4j
@Service
public class SendOrderConsentForApprovedOrderSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrcService;
    private final ConsentSendOrderCorresponder consentSendOrderCorresponder;

    public SendOrderConsentForApprovedOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
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
            && EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                               String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<SendCorrespondenceEvent> events = consentSendOrderCorresponder
            .buildApprovedOrderEvents(caseDetails, caseDetailsBefore, userAuthorisation);
        List<String> errors = new ArrayList<>();
        for (SendCorrespondenceEvent event : events) {
            String actionName = "Send consent order correspondence to " + event.describeNotificationParties();
            boolean sent = correspondenceEventAuditOrcService.publishEvent(event, actionName);
            if (!sent) {
                errors.add("Failed to send correspondence to " + event.describeNotificationParties());
            }
        }

        if (!errors.isEmpty()) {
            return submittedResponse(
                toConfirmationHeader("Send order event submitted with errors"),
                toConfirmationBody(errors.toArray(new String[0]))
            );
        } else {
            return submittedResponse();
        }
    }
}
