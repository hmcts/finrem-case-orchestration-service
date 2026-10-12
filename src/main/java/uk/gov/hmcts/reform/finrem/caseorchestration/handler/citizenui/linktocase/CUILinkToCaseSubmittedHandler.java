package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.CorrespondenceSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

public abstract class CUILinkToCaseSubmittedHandler
    extends CorrespondenceSubmittedHandler {

    private final SignInConfirmationCorresponder signInConfirmationCorresponder;

    protected CUILinkToCaseSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                            EvidenceManagementDeleteService evidenceManagementDeleteService,
                                            RetryExecutor retryExecutor, SignInConfirmationCorresponder signInConfirmationCorresponder,
                                            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
            correspondenceEventAuditOrchestrationService);

        this.signInConfirmationCorresponder = signInConfirmationCorresponder;
    }

    @Override
    protected SendCorrespondenceEvent buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest,
                                                                   String userAuthorisation) {
        return signInConfirmationCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty()
        );
    }

    @Override
    protected String correspondenceTaskDescription() {
        return String.format("Send sign in confirmation email: %s", notificationParty().getRole());
    }

    /**
     * Returns the target citizen party for the handler implementation.
     *
     * @return notification party (citizen applicant or citizen respondent)
     */
    protected abstract NotificationParty notificationParty();
}
