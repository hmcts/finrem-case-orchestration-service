package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;

/**
 * class for sending "Assign to Judge" notifications to all relevant parties in a financial remedy case.
 *
 * <p>
 * This class handles the orchestration of notifications and letter generation for the Assign to Judge event,
 * including emails to solicitors and letters for bulk print. Subclasses can specialise behaviour such as skipping
 * letters to international respondents (e.g. {@code FinremAssignToJudgeCorresponder} was previously used for Issue Application,
 * but that use case avoids sending letters to international respondents).
 * </p>
 *
 * <p>
 * This class extends {@link FinremSingleLetterOrEmailAllPartiesCorresponder}, and uses the
 * {@link AssignedToJudgeDocumentService} to generate the relevant documents.
 * </p>
 *
 * @see AssignedToJudgeDocumentService
 */
@Component
@Slf4j
public class AssignToJudgeCorresponder {

    private static final EmailTemplateNames EMAIL_TEMPLATE = FR_ASSIGNED_TO_JUDGE;

    private final NotificationService notificationService;

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    protected AssignToJudgeCorresponder(NotificationService notificationService,
                                        AssignedToJudgeDocumentService assignedToJudgeDocumentService,
                                        FinremNotificationRequestMapper finremNotificationRequestMapper,
                                        CheckSolicitorIsDigitalService checkSolicitorIsDigitalService) {
        this.notificationService = notificationService;
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
        this.finremNotificationRequestMapper = finremNotificationRequestMapper;
        this.checkSolicitorIsDigitalService = checkSolicitorIsDigitalService;
    }

    public List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(FinremCaseDetails finremCaseDetails,
                                                                       String authToken) {
        return buildSendCorrespondenceEvents(finremCaseDetails, false, authToken);
    }

    public List<SendCorrespondenceEvent> buildSendCorrespondenceEventsForAuditCreation(FinremCaseDetails finremCaseDetails,
                                                                                       String authToken) {
        return buildSendCorrespondenceEvents(finremCaseDetails, true, authToken);
    }

    // replacing FinremSingleLetterOrEmailAllPartiesCorresponder.sendCorrespondence
    private List<SendCorrespondenceEvent> buildSendCorrespondenceEvents(FinremCaseDetails finremCaseDetails, boolean doNotGenerateReport,
                                                                       String authToken) {
        List<SendCorrespondenceEvent> events = new ArrayList<>();
        events.add(
            // replacing sendApplicantCorrespondence
            SendCorrespondenceEvent.builder()
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
                .caseDetails(finremCaseDetails)
                .notificationParties(List.of(NotificationParty.RESPONDENT))
                .emailTemplate(EMAIL_TEMPLATE)
                .emailNotificationRequest(getRespondentEmailNotificationRequest(finremCaseDetails))
                .documentsToPost(List.of(
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
                        .caseDetails(finremCaseDetails)
                        .notificationParties(List.of(NotificationParty.getNotificationPartyFromRole(intervenerWrapper
                            .getIntervenerSolicitorCaseRole().name())))
                        .emailTemplate(EMAIL_TEMPLATE)
                        .emailNotificationRequest(getIntervenerEmailNotificationRequest(finremCaseDetails,
                            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper)))
                        .documentsToPost(List.of(
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
