package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;

/**
 * Abstract base class for sending "Assign to Judge" notifications to all relevant parties in a financial remedy case.
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
 * @see uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder
 * @see uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService
 */
@Component
@Slf4j
public abstract class AbstractAssignToJudgeCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    private final AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    private final FinremNotificationRequestMapper finremNotificationRequestMapper;

    private final CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    protected AbstractAssignToJudgeCorresponder(NotificationService notificationService,
                                                BulkPrintService bulkPrintService,
                                                AssignedToJudgeDocumentService assignedToJudgeDocumentService,
                                                FinremNotificationRequestMapper finremNotificationRequestMapper,
                                                CheckSolicitorIsDigitalService checkSolicitorIsDigitalService) {
        super(notificationService, bulkPrintService);
        this.assignedToJudgeDocumentService = assignedToJudgeDocumentService;
        this.finremNotificationRequestMapper = finremNotificationRequestMapper;
        this.checkSolicitorIsDigitalService = checkSolicitorIsDigitalService;
    }

    @Override
    protected EmailTemplateNames getApplicantEmailTemplate() {
        return FR_ASSIGNED_TO_JUDGE;
    }

    @Override
    protected EmailTemplateNames getRespondentEmailTemplate() {
        return FR_ASSIGNED_TO_JUDGE;
    }

    @Override
    protected NotificationRequest getApplicantEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(caseDetails, !isApplicantSolicitorDigital(caseDetails));
    }

    @Override
    protected NotificationRequest getRespondentEmailNotificationRequest(FinremCaseDetails caseDetails) {
        return finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(caseDetails, !isRespondentSolicitorDigital(caseDetails));
    }

    private boolean isApplicantSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isApplicantSolicitorDigital(caseDetails.getId().toString());
    }

    private boolean isRespondentSolicitorDigital(FinremCaseDetails caseDetails) {
        return checkSolicitorIsDigitalService.isRespondentSolicitorDigital(caseDetails.getId().toString());
    }

    @Override
    protected boolean shouldSendIntervenerLetter(IntervenerWrapper intervenerWrapper) {
        return intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty();
    }

    @Override
    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorEmailPopulated(caseDetails);
    }

    @Override
    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorEmailPopulated(caseDetails);
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(
            caseDetails, authorisationToken, recipient);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        notificationService.sendAssignToJudgeConfirmationEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
