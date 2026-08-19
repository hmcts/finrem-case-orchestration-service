package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented.AbstractDefaultIssueApplicationSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HwfAcceptedAndIssueSubmittedHandler extends AbstractDefaultIssueApplicationSubmittedHandler {

    private static final String CONFIRMATION_HEADER_WITH_ERROR = "HWF accepted and issued with errors";

    private final HwfCorrespondenceService hwfNotificationsService;

    public HwfAcceptedAndIssueSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                               EvidenceManagementDeleteService evidenceManagementDeleteService,
                                               RetryExecutor retryExecutor,
                                               IssueApplicationConsentCorresponder issueApplicationConsentCorresponder,
                                               AssignPartiesAccessService assignPartiesAccessService,
                                               HwfCorrespondenceService hwfNotificationsService) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService,
            retryExecutor, issueApplicationConsentCorresponder, assignPartiesAccessService);
        this.hwfNotificationsService = hwfNotificationsService;
    }

    @Override
    protected EventType supportedEventType() {
        return EventType.HWF_ACCEPTED_AND_ISSUE;
    }

    protected String getConfirmationHeader() {
        return CONFIRMATION_HEADER_WITH_ERROR;
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
