package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

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

        if (caseDetails.getData().isContestedApplication()) {
            FinremCaseDataContested data = (FinremCaseDataContested) caseDetails.getData();
            List<AdditionalHearingDocumentCollection> additionalHearingDocuments = data.getAdditionalHearingDocuments();

            if (additionalHearingDocuments != null && !additionalHearingDocuments.isEmpty()) {
                AdditionalHearingDocumentCollection additionalHearingDocumentCollection =
                    additionalHearingDocuments.get(additionalHearingDocuments.size() - 1);
                BulkPrintDocument additionalDoc
                    = documentHelper.getBulkPrintDocumentFromCaseDocument(additionalHearingDocumentCollection.getValue().getDocument());
                documents.add(additionalDoc);
            }

            if (data.getAdditionalListOfHearingDocuments() != null) {
                BulkPrintDocument additionalUploadedDoc
                    = documentHelper.getBulkPrintDocumentFromCaseDocument(data.getAdditionalListOfHearingDocuments());
                documents.add(additionalUploadedDoc);
            }
        }
        return documents;
    }
}
