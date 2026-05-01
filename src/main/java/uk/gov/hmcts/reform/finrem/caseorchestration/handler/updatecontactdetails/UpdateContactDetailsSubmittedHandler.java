package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateContactDetailsNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class UpdateContactDetailsSubmittedHandler extends FinremCallbackHandler {

    private class SolicitorAccessChangeResult {
        private boolean applicantSolicitorGranted;
        private boolean applicantSolicitorRevoked;
        private boolean respondentSolicitorGranted;
        private boolean respondentSolicitorRevoked;

        public boolean anySolicitorAccessChanged() {
            return applicantSolicitorGranted
                || applicantSolicitorRevoked
                || respondentSolicitorGranted
                || respondentSolicitorRevoked;
        }
    }

    private static final String CONFIRMATION_HEADER_WITH_ERROR = "Contact details updated with errors";
    private final UpdateContactDetailsNotificationService updateContactDetailsNotificationService;
    private final AssignPartiesAccessService assignPartiesAccessService;
    private final RetryExecutor retryExecutor;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UpdateContactDetailsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                UpdateContactDetailsNotificationService updateContactDetailsNotificationService,
                                                AssignPartiesAccessService assignPartiesAccessService,
                                                RetryExecutor retryExecutor,
                                                ApplicationEventPublisher applicationEventPublisher) {
        super(finremCaseDetailsMapper);
        this.updateContactDetailsNotificationService = updateContactDetailsNotificationService;
        this.assignPartiesAccessService = assignPartiesAccessService;
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

        List<String> errors = new ArrayList<>();

        SolicitorAccessChangeResult result = checkAndAssignSolicitorAccess(callbackRequest, errors);

        if (result.anySolicitorAccessChanged()) {
            // Assumes only one litigant party changes within this notification logic.
            // Based on the question: "Does this update include a change in representation for either party?"
            // Notifications will still be sent if the case assignment is only partially completed (e.g.,
            // applicant solicitor granted but revocation of the previous solicitor failed).
            // Further enhancement can refine notification accuracy if needed.
            List<SendCorrespondenceEvent> events = prepareNocEmailToLitigantSolicitor(caseDetails);
            sendNocEmailToLitigantSolicitorWithRetry(events, errors);
            sendNocLetterToLitigantsWithRetry(caseDetails, caseDetailsBefore, userAuthorisation, errors);
        }

        if (!errors.isEmpty()) {
            return submittedResponse(
                toConfirmationHeader(CONFIRMATION_HEADER_WITH_ERROR),
                toConfirmationBody(errors.toArray(new String[0]))
            );
        }
        return submittedResponse();
    }

    private boolean shouldProceedRespondentSolicitorCaseAssignment(FinremCallbackRequest callbackRequest) {
        return callbackRequest.hasRespondentSolicitorChanged()
            && hasApplicationBeenIssued(callbackRequest.getFinremCaseData());
    }

    private boolean shouldProceedApplicantSolicitorCaseAssignment(FinremCallbackRequest callbackRequest) {
        return callbackRequest.hasApplicantSolicitorChanged();
    }

    /**
     *  Checks if the application has been issued by verifying if the issue date is present in the case details.
     *  If the issue date is not null, it indicates that the application has been issued.
     */
    private boolean hasApplicationBeenIssued(FinremCaseData finremCaseData) {
        return !isNull(finremCaseData.getIssueDate());
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

    private SolicitorAccessChangeResult checkAndAssignSolicitorAccess(FinremCallbackRequest callbackRequest, List<String> errors) {
        FinremCaseData caseData = callbackRequest.getFinremCaseData();
        FinremCaseData caseDataBefore = callbackRequest.getFinremCaseDataBefore();

        SolicitorAccessChangeResult result = new SolicitorAccessChangeResult();

        if (shouldProceedApplicantSolicitorCaseAssignment(callbackRequest)) {
            String newEmail = caseData.getAppSolicitorEmailIfRepresented();
            String oldEmail = caseDataBefore.getAppSolicitorEmailIfRepresented();

            if (StringUtils.isNotBlank(newEmail)) {
                result.applicantSolicitorGranted = retryExecutor.supplyWithRetryWithHandler(
                    () -> {
                        assignPartiesAccessService.grantApplicantSolicitor(caseData);
                        return true;
                    },
                    "Update Contact Details - granting applicant solicitor",
                    caseData.getCcdCaseId(),
                    (exception, actionName, caseId) ->
                        errors.add("There was a problem granting access to applicant solicitor (%s). Please grant access manually."
                            .formatted(newEmail))
                ).orElse(false);
            }
            if (StringUtils.isNotBlank(oldEmail)) {
                result.applicantSolicitorRevoked = retryExecutor.supplyWithRetryWithHandler(
                    () -> {
                        assignPartiesAccessService.revokeApplicantSolicitor(caseDataBefore);
                        return true;
                    },
                    "Update Contact Details - revoking applicant solicitor",
                    caseData.getCcdCaseId(),
                    (exception, actionName, caseId) ->
                        errors.add("There was a problem revoking access to applicant solicitor (%s). Please revoke access manually."
                            .formatted(oldEmail))
                ).orElse(false);
            }
        }

        if (shouldProceedRespondentSolicitorCaseAssignment(callbackRequest)) {
            // TODO @Dawud will do the respondent part.
        }

        return result;
    }
}
