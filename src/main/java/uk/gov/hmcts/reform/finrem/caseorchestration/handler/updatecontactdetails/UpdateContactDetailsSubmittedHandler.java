package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SolicitorAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class UpdateContactDetailsSubmittedHandler extends FinremCallbackHandler {
    private final SolicitorAccessService solicitorAccessService;
    private final RetryExecutor retryExecutor;

    public UpdateContactDetailsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                SolicitorAccessService solicitorAccessService, RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.solicitorAccessService = solicitorAccessService;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        String checkAndAssignSolicitorAccessError = checkAndAssignSolicitorAccess(callbackRequest);

        if (StringUtils.isNotBlank(checkAndAssignSolicitorAccessError)) {
            return submittedResponse(toConfirmationHeader("Update contact details with Errors"),
                toConfirmationBody(checkAndAssignSolicitorAccessError));
        }

        // Check if the update includes a representative change and send Notice of Change notifications if required
        solicitorAccessService.sendNoticeOfChangeNotificationsCaseworker(callbackRequest, userAuthorisation);

        return submittedResponse();
    }

    private String checkAndAssignSolicitorAccess(FinremCallbackRequest callbackRequest) {

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        caseDetailsBefore.getData().setCcdCaseId(caseDetailsBefore.getCaseIdAsString());
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        try {
            retryExecutor.runWithRetry(() -> solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore),
                "Update Contact Details - Case Solicitor Change",
                caseData.getCcdCaseId()
            );
            return null;
        } catch (Exception ex) {
            log.error("Error updating solicitor access to case", ex);
            return "There was a problem updating solicitor access to case.";
        }
    }
}
