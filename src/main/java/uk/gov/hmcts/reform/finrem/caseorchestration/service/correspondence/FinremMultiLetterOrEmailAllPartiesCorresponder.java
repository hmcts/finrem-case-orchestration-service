package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
public abstract class FinremMultiLetterOrEmailAllPartiesCorresponder extends MultiLetterOrEmailAllPartiesCorresponder<FinremCaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    protected void sendApplicantCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendRespondentCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
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


}
