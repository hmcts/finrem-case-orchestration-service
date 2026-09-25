package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractIssueApplicationSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HwfAcceptedAndIssueSubmittedHandler extends AbstractIssueApplicationSubmittedHandler {

    private final HwfCorrespondenceService hwfNotificationsService;

    public HwfAcceptedAndIssueSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               HwfCorrespondenceService hwfNotificationsService,
                                               EvidenceManagementDeleteService evidenceManagementDeleteService,
                                               RetryExecutor retryExecutor,
                                               AssignToJudgeCorresponder assignToJudgeCorresponder,
                                               AssignPartiesAccessService assignPartiesAccessService,
                                               ApplicationEventPublisher applicationEventPublisher,
                                               NotificationAuditService notificationAuditService,
                                               CoreCaseDataService coreCaseDataService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor, assignToJudgeCorresponder,
            assignPartiesAccessService, applicationEventPublisher, notificationAuditService, coreCaseDataService);
        this.hwfNotificationsService = hwfNotificationsService;
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.HWF_ACCEPTED_AND_ISSUE;
    }

    @Override
    protected String getConfirmationHeader() {
        return "HWF accepted and issued with errors";
    }

    private String sendHwfCorrespondence(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        AtomicReference<String> error = new AtomicReference<>();
        retryExecutor.runWithRetryWithHandler(() -> hwfNotificationsService.sendCorrespondence(finremCaseDetails, userAuthorisation),
            "sending HWF correspondence", finremCaseDetails.getCaseIdAsString(),
            (exception, actionName, caseId1) ->
                error.set("There was a problem sending HWF correspondence. Please send it manually."));
        return error.get();
    }

    @Override
    protected List<SubmittedTask> additionalTasks() {
        return List.of(this::sendHwfCorrespondence);
    }
}
