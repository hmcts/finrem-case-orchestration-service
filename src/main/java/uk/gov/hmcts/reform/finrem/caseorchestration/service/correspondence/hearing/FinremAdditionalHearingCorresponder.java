package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FinremAdditionalHearingCorresponder extends FinremHearingCorresponder {

    @Autowired
    public FinremAdditionalHearingCorresponder(BulkPrintService bulkPrintService,
                                               NotificationService notificationService,
                                               DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, documentHelper);
    }

    @Override
    public List<CaseDocument> getCaseDocuments(FinremCaseDetails caseDetails) {

        List<CaseDocument> documents = new ArrayList<>();
        List<AdditionalHearingDocumentCollection> additionalHearingDocuments = caseDetails.getData().getAdditionalHearingDocuments();

        if (additionalHearingDocuments != null && !additionalHearingDocuments.isEmpty()) {
            Collections.sort(additionalHearingDocuments, (o1, o2) -> o2.getValue().getAdditionalHearingDocumentDate()
                    .compareTo(o1.getValue().getAdditionalHearingDocumentDate()));

            AdditionalHearingDocumentCollection additionalHearingDocumentCollection =
                additionalHearingDocuments.get(0);
            documents.add(additionalHearingDocumentCollection.getValue().getDocument());
        }

        CaseDocument additionalListOfHearingDocuments = caseDetails.getData().getAdditionalListOfHearingDocuments();
        if (additionalListOfHearingDocuments != null) {
            documents.add(additionalListOfHearingDocuments);
        }

        return documents;
    }
}
