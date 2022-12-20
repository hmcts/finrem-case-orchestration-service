package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing;

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
public class AdditionalHearingApplicantCorresponder extends MultiLetterOrEmailApplicantCorresponder {

    private final AdditionalHearingDocumentGenerator additionalHearingDocumentService;

    @Autowired
    public AdditionalHearingApplicantCorresponder(NotificationService notificationService,
                                                  BulkPrintService bulkPrintService,
                                                  AdditionalHearingDocumentGenerator additionalHearingDocumentService) {
        super(notificationService, bulkPrintService);
        this.additionalHearingDocumentService = additionalHearingDocumentService;
    }

    @Override
    protected void emailSolicitor(CaseDetails caseDetails) {
        notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        return additionalHearingDocumentService.generateDocuments(caseDetails);
    }
}
