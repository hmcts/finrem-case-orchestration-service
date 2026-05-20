package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremSubmittedCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeneralApplicationDirectionsSubmittedHandler extends FinremSubmittedCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;
    private final GeneralApplicationDirectionsService generalApplicationDirectionsService;

    public GeneralApplicationDirectionsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        EvidenceManagementDeleteService evidenceManagementDeleteService,
                                                        ManageHearingsCorresponder manageHearingsCorresponder,
                                                        GeneralApplicationDirectionsService generalApplicationDirectionsService,
                                                        RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper, evidenceManagementDeleteService, retryExecutor);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
        this.generalApplicationDirectionsService = generalApplicationDirectionsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.GENERAL_APPLICATION_DIRECTIONS_MH.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        // Hearings are optional, so send hearing correspondence if a hearing was added in the event.
        if (generalApplicationDirectionsService.isHearingRequired(finremCaseDetails)) {
            List<String> errors = new ArrayList<>();
            retryExecutor.runWithRetryWithHandler(
                () -> manageHearingsCorresponder.sendHearingCorrespondence(callbackRequest, userAuthorisation),
                "Send Hearing Correspondence",
                finremCaseDetails.getCaseIdAsString(),
                (exception, actionName, caseId1) ->
                    errors.add("Fail to send hearing correspondence.")
            );
            if (!errors.isEmpty()) {
                return submittedResponse(
                    toConfirmationHeader("General application directions submitted with errors"),
                    toConfirmationBody(errors.toArray(new String[0]))
                );
            }
        }
        return submittedResponse();
    }
}
