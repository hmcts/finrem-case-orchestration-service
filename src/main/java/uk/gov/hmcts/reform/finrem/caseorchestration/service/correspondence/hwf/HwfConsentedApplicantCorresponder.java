package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsSingleLetterOrEmailApplicantCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@Component
@Slf4j
public class HwfConsentedApplicantCorresponder extends CaseDetailsSingleLetterOrEmailApplicantCorresponder {

    private final HelpWithFeesDocumentService helpWithFeesDocumentService;

    @Autowired
    public HwfConsentedApplicantCorresponder(BulkPrintService bulkPrintService,
                                             NotificationService notificationService,
                                             HelpWithFeesDocumentService helpWithFeesDocumentService) {
        super(bulkPrintService, notificationService);
        this.helpWithFeesDocumentService = helpWithFeesDocumentService;
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken) {
        log.info("Getting HWF Successful notification letter for bulk print");
        return helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(
            caseDetails, authorisationToken, APPLICANT);

    }


    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending Consented HWF Successful email notification to Solicitor");
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }

}
