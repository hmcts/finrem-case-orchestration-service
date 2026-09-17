package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.Optional;

@Slf4j
@Service
public class CUIRespondentDocumentUploadSubmittedHandler extends CUIDocumentUploadSubmittedHandler {

    public CUIRespondentDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                       RetryExecutor retryExecutor,
                                                       NotificationService notificationService,
                                                       CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
            notificationService, correspondenceEventAuditOrchestrationService);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CUI_RESPONDENT_DOCUMENT_UPLOAD.equals(eventType);
    }

    @Override
    protected Optional<SendCorrespondenceEvent> buildSendCorrespondenceEvent(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        return notificationService.buildCitizenRespondentUploadDocumentsNotificationEvent(
            callbackRequest.getCaseDetails(), userAuthorisation);
    }

    @Override
    protected String noRecipientWarningMessage() {
        return "No recipient email found for citizen respondent upload notification";
    }

    @Override
    protected String correspondenceTaskDescription() {
        return "Send citizen respondent upload documents correspondence";
    }

    @Override
    protected String markAuditsActionName() {
        return "markCuiRespondentNotificationAuditAsSent";
    }
}
