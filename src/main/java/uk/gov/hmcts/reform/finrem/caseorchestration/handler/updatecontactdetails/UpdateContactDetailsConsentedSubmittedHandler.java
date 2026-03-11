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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UpdateContactDetailsConsentedSubmittedHandler extends FinremCallbackHandler {
    private final SolicitorAccessService solicitorAccessService;

    public UpdateContactDetailsConsentedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         SolicitorAccessService solicitorAccessService) {
        super(finremCaseDetailsMapper);
        this.solicitorAccessService = solicitorAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        String checkAndAssignSolicitorAccessError = checkAndAssignSolicitorAccess(callbackRequest);

        if (StringUtils.isAllBlank(checkAndAssignSolicitorAccessError)) {
            return submittedResponse("# Updated Case Solicitor with Errors",
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
        List<String> errors = new ArrayList<>();

        try {
            executeWithRetry(log,
                () -> solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors),
                callbackRequest.getCaseDetails().getCaseIdAsString(),
                "Update Contact Details - Case Solicitor Change",
                3
            );
            return null;
        } catch (Exception ex) {
            return errors.getFirst();
        }
    }
}

