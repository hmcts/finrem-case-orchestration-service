package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsSingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase<CaseDetails> {

    protected final NotificationService notificationService;
    protected final BulkPrintService bulkPrintService;
    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (notificationService.isContestedApplication(caseDetails)) {
            sendIntervenerCorrespondence(caseDetails);
        }
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void sendApplicantCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails, APPLICANT, authorisationToken);
        }
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void sendRespondentCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails, RESPONDENT, authorisationToken);
        }
    }

    protected void sendIntervenerCorrespondence(CaseDetails caseDetails) {
        final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        final List<IntervenerWrapper> interveners =  finremCaseDetails.getData().getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for case: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
            }
        });
    }

    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);


    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(CaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails);
}
