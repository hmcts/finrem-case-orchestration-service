package uk.gov.hmcts.reform.finrem.caseorchestration.handler.consented;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.AssignToJudgeCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

import java.util.List;

import static java.util.Objects.nonNull;

public abstract class AbstractIssueApplicationAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final NotificationAuditService notificationAuditService;

    private final OnlineFormDocumentService onlineFormDocumentService;

    private final IssueApplicationService issueApplicationService;

    protected final AssignToJudgeCorresponder assignToJudgeCorresponder;

    private static final String MISSING_COURT_SELECTION_ERROR = "Case cannot be issued as court selection is missing.";

    protected AbstractIssueApplicationAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           OnlineFormDocumentService onlineFormDocumentService,
                                                           IssueApplicationService issueApplicationService,
                                                           NotificationAuditService notificationAuditService,
                                                           AssignToJudgeCorresponder assignToJudgeCorresponder) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.issueApplicationService = issueApplicationService;
        this.notificationAuditService = notificationAuditService;
        this.assignToJudgeCorresponder = assignToJudgeCorresponder;
    }

    protected abstract EventType supportedEventType();

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && supportedEventType().equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        try {
            generateCoverSheets(caseDetails, userAuthorisation);
        } catch (MissingCourtException e) {
            return responseWithoutWarnings(caseData, List.of(MISSING_COURT_SELECTION_ERROR));
        }

        caseData.setMiniFormA(onlineFormDocumentService.generateMiniFormA(userAuthorisation, caseDetails));
        populateAssignToJudgeFields(caseData);

        createAuditsForCorrespondence(callbackRequest, userAuthorisation);

        return response(caseData);
    }

    private void createAuditsForCorrespondence(FinremCallbackRequest callbackRequest,
                                               String userAuthorisation) {
        List<SendCorrespondenceEvent> events = assignToJudgeCorresponder
            .buildSendCorrespondenceEvents(callbackRequest.getCaseDetails(), userAuthorisation);
        String trackerId = null;
        for (SendCorrespondenceEvent event : events) {
            if (nonNull(trackerId)) {
                event.setNotificationTrackerId(trackerId);
            }
            trackerId = notificationAuditService.createAuditsForCorrespondence(event, callbackRequest.getEventType());
        }
    }

    private void populateAssignToJudgeFields(FinremCaseData caseData) {
        issueApplicationService.populateAssignToJudgeFields(caseData);
    }

    private void generateCoverSheets(FinremCaseDetails caseDetails, String userAuthorisation) {
        issueApplicationService.generateCoverSheets(caseDetails, userAuthorisation);
    }
}
