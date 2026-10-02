package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class CUIApplicantDocumentUploadSubmittedHandler extends CUIDocumentUploadSubmittedHandler {

    public CUIApplicantDocumentUploadSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                      RetryExecutor retryExecutor,
                                                      CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                      CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
            citizenDocumentsUploadedCorresponder, correspondenceEventAuditOrchestrationService);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.CUI_APPLICANT_DOCUMENT_UPLOAD.equals(eventType);
    }

    @Override
    protected NotificationParty notificationParty() {
        return NotificationParty.CITIZEN_APPLICANT;
    }
}
