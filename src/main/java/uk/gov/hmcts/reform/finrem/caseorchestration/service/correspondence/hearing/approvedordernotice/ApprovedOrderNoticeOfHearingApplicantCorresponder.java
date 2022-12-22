package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.approvedordernotice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailApplicantCorresponder;

import java.util.List;

@Component
@Slf4j
public class ApprovedOrderNoticeOfHearingApplicantCorresponder extends MultiLetterOrEmailApplicantCorresponder {

    private final ApprovedOrderNoticeOfHearingDocumentsFetcher approvedOrderNoticeOfHearingDocumentsGenerator;

    @Autowired
    public ApprovedOrderNoticeOfHearingApplicantCorresponder(NotificationService notificationService,
                                                             BulkPrintService bulkPrintService,
                                                             ApprovedOrderNoticeOfHearingDocumentsFetcher
                                                                     approvedOrderNoticeOfHearingDocumentsGenerator) {
        super(notificationService, bulkPrintService);
        this.approvedOrderNoticeOfHearingDocumentsGenerator = approvedOrderNoticeOfHearingDocumentsGenerator;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        return approvedOrderNoticeOfHearingDocumentsGenerator.fetchDocuments(caseDetails);
    }

}
