package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class MultiLetterOrEmailApplicantCorresponder extends MultiLetterOrEmailCorresponderBase {

    @Autowired
    public MultiLetterOrEmailApplicantCorresponder(NotificationService notificationService,
                                                   BulkPrintService bulkPrintService) {
        super(notificationService, bulkPrintService);

    }

    @Override
    protected DocumentHelper.PaperNotificationRecipient getRecipient() {
        return DocumentHelper.PaperNotificationRecipient.APPLICANT;
    }

    @Override
    protected boolean shouldSendEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }
}
