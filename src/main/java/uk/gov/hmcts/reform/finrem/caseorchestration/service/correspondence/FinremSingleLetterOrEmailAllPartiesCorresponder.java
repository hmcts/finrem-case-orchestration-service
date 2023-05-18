package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole.INTVR_SOLICITOR_4;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremSingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase<FinremCaseDetails> {

    protected final NotificationService notificationService;
    protected final BulkPrintService bulkPrintService;

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        sendIntervenerCorrespondence(caseDetails, authToken);
    }

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT),
                caseDetails,
                CCDConfigConstant.APPLICANT);
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT),
                caseDetails,
                CCDConfigConstant.RESPONDENT);
        }
    }

    protected void sendIntervenerCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        if (shouldSendIntervenerSolicitorEmail(caseDetails, caseData.getIntervenerOneWrapper().getIntervener1SolEmail(), INTVR_SOLICITOR_1)) {
            log.info("Sending email correspondence to intervener 1 for case: {}", caseDetails.getId());
            SolicitorCaseDataKeysWrapper caseDataKeysWrapper = notificationService.getFinremCaseDataKeysForIntervenerOneSolicitor(caseData);
            this.emailIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,caseData.getIntervenerTwoWrapper().getIntervener2SolEmail(), INTVR_SOLICITOR_2)) {
            log.info("Sending email correspondence to intervener 2 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper caseDataKeysWrapper = notificationService.getFinremCaseDataKeysForIntervenerTwoSolicitor(caseData);
            this.emailIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,caseData.getIntervenerThreeWrapper().getIntervener3SolEmail(), INTVR_SOLICITOR_3)) {
            log.info("Sending email correspondence to intervener 3 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper caseDataKeysWrapper = notificationService.getFinremCaseDataKeysForIntervenerThreeSolicitor(caseData);
            this.emailIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,caseData.getIntervenerFourWrapper().getIntervener4SolEmail(), INTVR_SOLICITOR_4)) {
            log.info("Sending email correspondence to intervener 4 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper caseDataKeysWrapper = notificationService.getFinremCaseDataKeysForIntervenerFourSolicitor(caseData);
            this.emailIntervenerSolicitor(caseDetails, caseDataKeysWrapper);
        }
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(FinremCaseDetails caseDetails, String intervenerEmail, CaseRole caseRole) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerEmail, caseRole);
    }

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);


    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(FinremCaseDetails caseDetails, SolicitorCaseDataKeysWrapper caseDataKeysWrapper);
}
