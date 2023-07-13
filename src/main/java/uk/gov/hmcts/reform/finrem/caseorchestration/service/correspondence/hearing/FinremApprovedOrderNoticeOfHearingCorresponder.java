package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FinremApprovedOrderNoticeOfHearingCorresponder extends FinremHearingCorresponder {

    private final ObjectMapper objectMapper;
    private final DocumentHelper documentHelper;

    @Autowired
    public FinremApprovedOrderNoticeOfHearingCorresponder(BulkPrintService bulkPrintService,
                                                          NotificationService notificationService,
                                                          ObjectMapper objectMapper, DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService);
        this.objectMapper = objectMapper;
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails, String authorisationToken) {
        List<CaseDocument> hearingNoticePack = caseDetails.getData().getHearingNoticeDocumentPack().stream()
            .map(DocumentCollection::getValue)
            .collect(Collectors.toList());
        List<BulkPrintDocument> documentsToPrint = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);
        return documentsToPrint;
    }
}
