package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.CorrespondenceSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

/**
 * Submitted-stage handler for citizen document upload events.
 *
 * <p>Subclasses provide party-specific routing for applicant/respondent,
 * while the common correspondence publishing and audit reconciliation flow
 * is handled by {@link CorrespondenceSubmittedHandler}.</p>
 */
public abstract class CUIDocumentUploadSubmittedHandler extends CorrespondenceSubmittedHandler {

    private final CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;

    protected CUIDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                RetryExecutor retryExecutor,
                                                CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
            correspondenceEventAuditOrchestrationService);
        this.citizenDocumentsUploadedCorresponder = citizenDocumentsUploadedCorresponder;
    }

    @Override
    protected SendCorrespondenceEvent buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        return citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty());
    }

    @Override
    protected String correspondenceTaskDescription() {
        return String.format(
            "Send citizen documents uploaded email: %s",
            notificationParty().getRole());
    }

    /**
     * Returns the target citizen party for the handler implementation.
     *
     * @return notification party (citizen applicant or citizen respondent)
     */
    protected abstract NotificationParty notificationParty();
}
