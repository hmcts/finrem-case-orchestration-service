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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IssueApplicationContestedSubmittedHandler extends FinremCallbackHandler {

    private final IssueApplicationContestedEmailCorresponder corresponder;

    private final AssignPartiesAccessService assignPartiesAccessService;

    public IssueApplicationContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     IssueApplicationContestedEmailCorresponder corresponder,
                                                     AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.corresponder = corresponder;
        this.assignPartiesAccessService = assignPartiesAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.ISSUE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        validateCaseData(callbackRequest);

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        final String caseIdAsString = caseDetails.getCaseIdAsString();

        grantRespondentSolicitor(caseData, caseIdAsString, 3);
        sendCorrespondenceWithRetry(caseDetails, caseIdAsString, 3);

        return response(caseData);
    }

    private void grantRespondentSolicitor(FinremCaseData caseData, String caseIdAsString, int retriesLeft) {
        CompletableFuture.runAsync(() -> {
            try {
                assignPartiesAccessService.grantRespondentSolicitor(caseData);

            } catch (Exception e) {
                log.error("{} - Failed granting respondent solicitor. Retries left: {}",
                    caseIdAsString, retriesLeft, e);

                if (retriesLeft > 0) {
                    grantRespondentSolicitor(caseData, caseIdAsString, retriesLeft - 1);
                } else {
                    log.error("{} - All retry attempts exhausted while granting respondent solicitor",
                        caseIdAsString);
                }
            }
        }, CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS));
    }

    private void sendCorrespondenceWithRetry(FinremCaseDetails caseDetails, String caseIdAsString,
                                             int retriesLeft) {

        CompletableFuture.runAsync(() -> {
            try {
                corresponder.sendCorrespondence(caseDetails);

            } catch (Exception e) {
                log.error("{} - Failed sending correspondence. Retries left: {}",
                    caseIdAsString, retriesLeft, e);

                if (retriesLeft > 0) {
                    sendCorrespondenceWithRetry(caseDetails, caseIdAsString, retriesLeft - 1);
                } else {
                    log.error("{} - All retry attempts exhausted while sending correspondence",
                        caseIdAsString);
                }
            }
        }, CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS));
    }
}
