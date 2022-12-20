package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailApplicantCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.updatefrc.service.UpdateFrcInfoRespondentDocumentService;

@Component
@Slf4j
public class UpdateFrcApplicantCorresponder extends SingleLetterOrEmailApplicantCorresponder {

    private final UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService;

    @Autowired
    public UpdateFrcApplicantCorresponder(BulkPrintService bulkPrintService,
                                          NotificationService notificationService,
                                          UpdateFrcInfoRespondentDocumentService updateFrcInfoRespondentDocumentService) {
        super(notificationService, bulkPrintService);
        this.updateFrcInfoRespondentDocumentService = updateFrcInfoRespondentDocumentService;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendUpdateFrcInformationEmailToAppSolicitor(caseDetails);
    }

    @Override
    public CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken, DocumentHelper.PaperNotificationRecipient recipient) {
        return updateFrcInfoRespondentDocumentService.generateSolicitorUpdateFrcInfoLetter(caseDetails, authorisationToken, recipient);
    }
}
