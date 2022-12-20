package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HelpWithFeesDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailApplicantCorresponder;

@Component
@Slf4j
public class HwfConsentedApplicantCorresponder extends SingleLetterOrEmailApplicantCorresponder {

    private final HelpWithFeesDocumentService helpWithFeesDocumentService;

    @Autowired
    public HwfConsentedApplicantCorresponder(BulkPrintService bulkPrintService,
                                             NotificationService notificationService, HelpWithFeesDocumentService helpWithFeesDocumentService) {
        super(notificationService, bulkPrintService);
        this.helpWithFeesDocumentService = helpWithFeesDocumentService;
    }


    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken, DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Getting HWF Successful notification letter for bulk print");
        return helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(
            caseDetails, authorisationToken, recipient);

    }


    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        log.info("Sending Consented HWF Successful email notification to Solicitor");
        notificationService.sendConsentedHWFSuccessfulConfirmationEmail(caseDetails);
    }

}
