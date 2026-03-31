package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UpdateContactDetailsSubmittedHandler extends FinremCallbackHandler {

    private static final String CONFIRMATION_HEADER_WITH_ERROR = "Contact details updated with Errors";

    private final UpdateContactDetailsNotificationService updateContactDetailsNotificationService;
    private final RetryExecutor retryExecutor;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UpdateContactDetailsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                UpdateContactDetailsNotificationService updateContactDetailsNotificationService,
                                                RetryExecutor retryExecutor,
                                                ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsNotificationService = updateContactDetailsNotificationService;
        this.retryExecutor = retryExecutor;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData finremCaseData = caseDetails.getData();

        /*
        DFR-4589 AUto assigning applicant solicitor logic here
        */

        if (requiresNotifications(finremCaseData)) {
            List<String> errors = new ArrayList<>();

            List<SendCorrespondenceEvent> events = prepareNocEmailToLitigantSolicitor(caseDetails);
            sendNocEmailToLitigantSolicitorWithRetry(events, errors);
            sendNocLetterToLitigantsWithRetry(caseDetails, caseDetailsBefore, userAuthorisation, errors);

            if (!errors.isEmpty()) {
                return submittedResponse(
                    toConfirmationHeader(CONFIRMATION_HEADER_WITH_ERROR),
                    toConfirmationBody(errors.toArray(new String[0]))
                );
            }
        }
        return submittedResponse();
    }

    private void sendNocEmailToLitigantSolicitorWithRetry(List<SendCorrespondenceEvent> events, List<String> errors) {
        events.forEach(event ->
            retryExecutor.runWithRetryWithHandler(
                () -> sendNocEmailToLitigantSolicitor(event),
                "Sending NOC email to litigant solicitor",
                event.getCaseId(),
                (exception, actionName, caseId1) ->
                    errors.add("Fail to send notice of change email to litigant solicitor.")
            )
        );
    }

    private void sendNocLetterToLitigantsWithRetry(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore,
                                                     String userAuthorisation, List<String> errors) {
        retryExecutor.runWithRetryWithHandler(
            () -> sendNocLetterToLitigants(finremCaseDetails, finremCaseDetailsBefore,
                userAuthorisation),
            "Sending NOC letter",
            finremCaseDetails.getCaseIdAsString(),
            (exception, actionName, caseId1) ->
                errors.add("Fail to send NOC letter to litigants.")
        );
    }

    private boolean requiresNotifications(FinremCaseData finremCaseData) {
        return updateContactDetailsNotificationService.requiresNotifications(finremCaseData);
    }

    private List<SendCorrespondenceEvent> prepareNocEmailToLitigantSolicitor(FinremCaseDetails caseDetails) {
        return List.of(
            updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(caseDetails)
        );
    }

    private void sendNocEmailToLitigantSolicitor(SendCorrespondenceEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    private void sendNocLetterToLitigants(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore,
                                          String userAuthorisation) {
        updateContactDetailsNotificationService.sendNocLetterToLitigants(finremCaseDetails, finremCaseDetailsBefore,
            userAuthorisation);
    }
}
