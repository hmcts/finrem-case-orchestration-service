package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;

@Component
@Slf4j
public class AdditionalHearingCorresponder extends HearingCorresponder {

    private final DocumentHelper documentHelper;

    @Autowired
    public AdditionalHearingCorresponder(BulkPrintService bulkPrintService,
                                         NotificationService notificationService,
                                         FinremCaseDetailsMapper finremCaseDetailsMapper,
                                         DocumentHelper documentHelper) {
        super(bulkPrintService, notificationService, finremCaseDetailsMapper);
        this.documentHelper = documentHelper;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails) {
        List<AdditionalHearingDocumentData> additionalHearingDocumentData =
            documentHelper.convertToAdditionalHearingDocumentData(
                caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));

        AdditionalHearingDocumentData additionalHearingDocument = additionalHearingDocumentData.get(additionalHearingDocumentData.size() - 1);

        List<BulkPrintDocument> documents = new ArrayList<>();
        if (caseDetails.getData().get(HEARING_ADDITIONAL_DOC) != null) {
            BulkPrintDocument additionalUploadedDoc
                = documentHelper.getBulkPrintDocumentFromCaseDocument(documentHelper
                .convertToCaseDocument(caseDetails.getData().get(HEARING_ADDITIONAL_DOC)));
            documents.add(additionalUploadedDoc);
        }

        BulkPrintDocument additionalDoc
            = documentHelper.getBulkPrintDocumentFromCaseDocument(additionalHearingDocument.getAdditionalHearingDocument().getDocument());

        documents.add(additionalDoc);
        return documents;
    }

}
