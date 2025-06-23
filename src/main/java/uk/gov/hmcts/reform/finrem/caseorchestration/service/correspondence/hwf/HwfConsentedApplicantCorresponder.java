package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailApplicantCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Component
@Slf4j
public class HwfConsentedApplicantCorresponder extends FinremSingleLetterOrEmailApplicantCorresponder {

    private final HelpWithFeesDocumentService helpWithFeesDocumentService;

    @Autowired
    public HwfConsentedApplicantCorresponder(BulkPrintService bulkPrintService,
                                             NotificationService notificationService,
                                             HelpWithFeesDocumentService helpWithFeesDocumentService) {
        super(bulkPrintService, notificationService);
        this.helpWithFeesDocumentService = helpWithFeesDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        log.info("Getting HWF Successful notification letter for bulk print");
        return helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(caseDetails, authorisationToken, APPLICANT);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("Sending Consented HWF Successful email notification to Solicitor");
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }

    /**
     * Determines whether a letter should be sent to the applicant.
     *
     * <p>
     * Applicants cannot be overseas and apply for Help With Fees (HWF),
     * as they must reside in the UK and receive UK benefits to be eligible.
     *
     * @param caseDetails the case details
     * @return {@code true} if the applicant resides in the UK and a letter should be sent; {@code false} otherwise
     */
    @Override
    public boolean shouldSendApplicantLetter(FinremCaseDetails caseDetails) {
        return isNotInternationalParty(getContactDetailsWrapper(caseDetails).getApplicantResideOutsideUK());
    }

}
