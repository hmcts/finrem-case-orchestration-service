package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FinremAdditionalHearingCorresponder extends FinremHearingCorresponder {

    private final DocumentHelper documentHelper;

    @Autowired
    public FinremAdditionalHearingCorresponder(BulkPrintService bulkPrintService,
                                               NotificationService notificationService,
                                               DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService);
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails) {
        List<BulkPrintDocument> documents = new ArrayList<>();

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = caseDetails.getData().getAdditionalHearingDocuments();
        AdditionalHearingDocument additionalHearingDocument = Optional.ofNullable(additionalHearingDocuments)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .reduce((first, second) -> second).get().getValue();

        if (additionalHearingDocument != null) {
            BulkPrintDocument additionalDoc
                = documentHelper.getBulkPrintDocumentFromCaseDocument(additionalHearingDocument.getDocument());
            documents.add(additionalDoc);
        }
        if (caseDetails.getData().getAdditionalListOfHearingDocuments() != null) {
            BulkPrintDocument additionalUploadedDoc
                = documentHelper.getBulkPrintDocumentFromCaseDocument(caseDetails.getData().getAdditionalListOfHearingDocuments());
            documents.add(additionalUploadedDoc);
        }

        return documents;
    }
}
