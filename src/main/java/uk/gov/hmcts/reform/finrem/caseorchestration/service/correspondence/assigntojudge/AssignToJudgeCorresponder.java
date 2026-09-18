package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;

@Component
@Slf4j
@RequiredArgsConstructor
public class AssignToJudgeCorresponder {

    private static final EmailTemplateNames EMAIL_TEMPLATE = FR_ASSIGNED_TO_JUDGE;

    private final NotificationService notificationService;

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    private final NotificationAuditService notificationAuditService;

    public void createAuditsForCorrespondence(EventType eventType, FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        List<SendCorrespondenceEvent> events = buildSendCorrespondenceEventsForAuditCreation(eventType, finremCaseDetails,
            userAuthorisation);
        String trackerId = null;
        for (SendCorrespondenceEvent event : events) {
            if (nonNull(trackerId)) {
                event.setNotificationTrackerId(trackerId);
            }
            trackerId = notificationAuditService.createAuditsForCorrespondence(event, eventType);
        }
    }

    public List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(EventType eventType, FinremCaseDetails finremCaseDetails,
                                                                       String authToken) {
        return buildSendCorrespondenceEvents(eventType, finremCaseDetails, false, authToken);
    }

    public List<SendCorrespondenceEvent> buildSendCorrespondenceEventsForAuditCreation(EventType eventType,
                                                                                       FinremCaseDetails finremCaseDetails,
                                                                                       String authToken) {
        return buildSendCorrespondenceEvents(eventType, finremCaseDetails, true, authToken);
    }

    // replacing FinremSingleLetterOrEmailAllPartiesCorresponder.sendCorrespondence
    private List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(EventType eventType,
                                                                        FinremCaseDetails finremCaseDetails,
                                                                        boolean doNotGenerateReport, String authToken) {
        List<SendCorrespondenceEvent> events = new ArrayList<>();
        events.add(
            // replacing sendApplicantCorrespondence
            SendCorrespondenceEvent.builder()
                .eventId(eventType.name())
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.APPLICANT))
                .emailTemplate(EMAIL_TEMPLATE)
                .emailNotificationRequest(getApplicantEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(doNotGenerateReport ? List.of() : List.of(
                    getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.APPLICANT)
                ))
                .authToken(authToken)
                .build()
        );
        events.add(
            // replacing sendRespondentCorrespondence
            SendCorrespondenceEvent.builder()
                .eventId(eventType.name())
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.RESPONDENT))
                .emailTemplate(EMAIL_TEMPLATE)
                .emailNotificationRequest(getRespondentEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(doNotGenerateReport ? List.of() : List.of(
                    getDocumentToPrint(finremCaseDetails, authToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT)
                ))
                .authToken(authToken)
                .build()
        );
        if (finremCaseDetails.isContestedApplication()) {
            // replacing sendIntervenerCorrespondence
            List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper ->
                events.add(
                    SendCorrespondenceEvent.builder()
                        .eventId(eventType.name())
                        .caseDetails(finremCaseDetails)
                        .notificationParties(List.of(NotificationParty.getNotificationPartyFromRole(intervenerWrapper
                            .getIntervenerSolicitorCaseRole().name())))
                        .emailTemplate(EMAIL_TEMPLATE)
                        .emailNotificationRequest(getIntervenerEmailNotificationRequest(finremCaseDetails,
                            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper)))
                        .documentsToPost(doNotGenerateReport ? List.of() : List.of(
                            getDocumentToPrint(finremCaseDetails, authToken, intervenerWrapper.getPaperNotificationRecipient())
                        ))
                        .authToken(authToken)
                        .build()
                )
            );
        }

        return events;
    }

    private NotificationRequest getApplicantEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, !isApplicantSolicitorDigital(caseDetails));
    }

    private NotificationRequest getRespondentEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, !isRespondentSolicitorDigital(caseDetails));
    }

    private NotificationRequest getIntervenerEmailNotificationRequest(FinremCaseDetails caseDetails,
                                                                      SolicitorCaseDataKeysWrapper dataKeysWrapper) {
        return finremNotificationRequestMapper
            .getNotificationRequestForIntervenerSolicitor(caseDetails, dataKeysWrapper);
    }

    private boolean isApplicantSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    private boolean isRespondentSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    private CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                            DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }
}
