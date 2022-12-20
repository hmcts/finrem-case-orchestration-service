package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.approvedordernotice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailRespondentCorresponder;

import java.util.List;

@Component
@Slf4j
public class ApprovedOrderNoticeOfHearingRespondentCorresponder extends MultiLetterOrEmailRespondentCorresponder {

    private final ApprovedOrderNoticeOfHearingDocumentsGenerator approvedOrderNoticeOfHearingDocumentsGenerator;

    @Autowired
    public ApprovedOrderNoticeOfHearingRespondentCorresponder(NotificationService notificationService,
                                                              BulkPrintService bulkPrintService,
                                                              ApprovedOrderNoticeOfHearingDocumentsGenerator approvedOrderNoticeOfHearingDocumentsGenerator) {
        super(notificationService, bulkPrintService);
        this.approvedOrderNoticeOfHearingDocumentsGenerator = approvedOrderNoticeOfHearingDocumentsGenerator;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
    }

    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        return approvedOrderNoticeOfHearingDocumentsGenerator.generateDocuments(caseDetails);
    }
}
