package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
public class FinremApprovedOrderNoticeOfHearingCorresponder extends FinremHearingCorresponder {

    private final DocumentHelper documentHelper;

    @Autowired
    public FinremApprovedOrderNoticeOfHearingCorresponder(BulkPrintService bulkPrintService,
                                                          NotificationService notificationService,
                                                          DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService);
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails) {
        List<CaseDocument> hearingNoticePack = null;
        if (caseDetails.getData().isContestedApplication()) {
            hearingNoticePack = ((FinremCaseDataContested) caseDetails.getData())
                .getHearingNoticeDocumentPack().stream()
                .map(DocumentCollection::getValue)
                .toList();
        }
        return documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);
    }
}
