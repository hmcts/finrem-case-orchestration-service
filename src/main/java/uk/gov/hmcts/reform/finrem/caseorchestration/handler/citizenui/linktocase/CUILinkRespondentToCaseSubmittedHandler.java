package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.linktocase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.SignInConfirmationCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class CUILinkRespondentToCaseSubmittedHandler extends CUILinkToCaseSubmittedHandler {

    public CUILinkRespondentToCaseSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                   EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                   RetryExecutor retryExecutor,
                                                   SignInConfirmationCorresponder signInConfirmationCorresponder,
                                                   CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor,
            signInConfirmationCorresponder, correspondenceEventAuditOrchestrationService);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LINK_APPLICANT_TO_CASE.equals(eventType);
    }

    @Override
    protected NotificationParty notificationParty() {
        return NotificationParty.CITIZEN_RESPONDENT;
    }
}
