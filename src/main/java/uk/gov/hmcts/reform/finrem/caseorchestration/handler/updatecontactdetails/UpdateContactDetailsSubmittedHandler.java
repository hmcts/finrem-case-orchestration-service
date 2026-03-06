package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
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
public class UpdateContactDetailsSubmittedHandler extends FinremCallbackHandler {

    private final SolicitorAccessService solicitorAccessService;


    public UpdateContactDetailsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                SolicitorAccessService solicitorAccessService) {
        super(finremCaseDetailsMapper);
        this.solicitorAccessService = solicitorAccessService;
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
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        caseDetailsBefore.getData().setCcdCaseId(caseDetailsBefore.getCaseIdAsString());
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        List<String> errors = new ArrayList<>();

        // Check for changes in solicitor details and update access accordingly
        solicitorAccessService.checkAndAssignSolicitorAccess(caseData, caseDataBefore, errors);

        // Check if the update includes a representative change and send Notice of Change notifications if required
        solicitorAccessService.sendNoticeOfChangeNotificationsCaseworker(callbackRequest, userAuthorisation);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }
}
