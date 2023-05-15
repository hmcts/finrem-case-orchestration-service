package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsMultiLetterOrEmailAllPartiesCorresponder extends MultiLetterOrEmailAllPartiesCorresponder<CaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    protected void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendIntervenerSolicitorEmail(caseDetails,"intervener1SolEmail", CaseRole.INTVR_SOLICITOR_1)) {
            log.info("Sending email correspondence to intervener 1 for case: {}", caseDetails.getId());
            SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper = notificationService.getCaseDataKeysForIntervenerOneSolicitor();
            this.emailIntervenerSolicitor(caseDetails, solicitorCaseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,"intervener2SolEmail", CaseRole.INTVR_SOLICITOR_2)) {
            log.info("Sending email correspondence to intervener 2 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper = notificationService.getCaseDataKeysForIntervenerTwoSolicitor();
            this.emailIntervenerSolicitor(caseDetails, solicitorCaseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,"intervener3SolEmail", CaseRole.INTVR_SOLICITOR_3)) {
            log.info("Sending email correspondence to intervener 3 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper = notificationService.getCaseDataKeysForIntervenerThreeSolicitor();
            this.emailIntervenerSolicitor(caseDetails, solicitorCaseDataKeysWrapper);
        }
        if (shouldSendIntervenerSolicitorEmail(caseDetails,"intervener4SolEmail", CaseRole.INTVR_SOLICITOR_4)) {
            log.info("Sending email correspondence to intervener 4 for case: {}", caseDetails.getId());
            final SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper = notificationService.getCaseDataKeysForIntervenerFourSolicitor();
            this.emailIntervenerSolicitor(caseDetails, solicitorCaseDataKeysWrapper);
        }
    }

    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(CaseDetails caseDetails, String intervenerField, CaseRole caseRole) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(caseDetails, intervenerField, caseRole);
    }

}
