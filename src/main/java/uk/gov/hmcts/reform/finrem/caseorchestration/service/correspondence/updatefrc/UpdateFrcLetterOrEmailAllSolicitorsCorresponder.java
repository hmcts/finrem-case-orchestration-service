package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

@Component
@Slf4j
public class UpdateFrcLetterOrEmailAllSolicitorsCorresponder extends CaseDetailsSingleLetterOrEmailAllPartiesCorresponder {

    private final UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;

    @Autowired
    public UpdateFrcLetterOrEmailAllSolicitorsCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                                           FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService) {
        super(notificationService, bulkPrintService, finremCaseDetailsMapper);
        this.updateFrcInfoRespondentDocumentService = updateFrcInfoRespondentDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken, DocumentHelper.PaperNotificationRecipient recipient) {
        return updateFrcInfoRespondentDocumentService.generateSolicitorUpdateFrcInfoLetter(caseDetails, authorisationToken, recipient);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'Update Frc information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(caseDetails);

    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'Update Frc information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor for 'Update Frc information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }

}
