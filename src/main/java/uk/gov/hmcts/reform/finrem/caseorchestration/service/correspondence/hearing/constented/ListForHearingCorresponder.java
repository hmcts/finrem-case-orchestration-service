package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.constented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

import java.util.List;

@Component
@Slf4j
public class ListForHearingCorresponder extends MultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public ListForHearingCorresponder(NotificationService notificationService,
                                      BulkPrintService bulkPrintService) {
        super(notificationService, bulkPrintService);
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        return null;
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {

    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {

    }
}
