package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
@SuppressWarnings({"java:S110","java:S1068","java:S2387"})
public class FinremApprovedOrderNoticeOfHearingCorresponder extends FinremHearingCorresponder {

    private final DocumentHelper documentHelper;

    @Autowired
    public FinremApprovedOrderNoticeOfHearingCorresponder(BulkPrintService bulkPrintService,
                                                          NotificationService notificationService,
                                                          DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, documentHelper);
        this.documentHelper = documentHelper;
    }

    @Override
    public List<CaseDocument> getCaseDocuments(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getHearingNoticeDocumentPack().stream()
            .map(DocumentCollection::getValue)
            .toList();
    }
}
