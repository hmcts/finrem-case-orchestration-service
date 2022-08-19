package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectGeneralApplicationSubmittedHandler implements CallbackHandler {

    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.REJECT_GENERAL_APPLICATION.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
        } else {
            paperNotificationService.printApplicantRejectionGeneralApplication(caseDetails, userAuthorisation);
        }

        if (notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToRepSolicitor(caseDetails);
        } else {
            paperNotificationService.printRespondentRejectionGeneralApplication(caseDetails, userAuthorisation);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }
}
