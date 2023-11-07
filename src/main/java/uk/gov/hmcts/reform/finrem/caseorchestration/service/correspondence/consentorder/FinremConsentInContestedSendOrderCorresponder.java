package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremMultiLetterOnlyAllPartiesCorresponder;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FinremConsentInContestedSendOrderCorresponder extends FinremMultiLetterOnlyAllPartiesCorresponder {

    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @Autowired
    public FinremConsentInContestedSendOrderCorresponder(NotificationService notificationService,
                                                         BulkPrintService bulkPrintService,
                                                         ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                         ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService) {
        super(bulkPrintService, notificationService);
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
    }

    @Override
    public List<BulkPrintDocument> getDocumentsToPrint(FinremCaseDetails caseDetails,
                                                       String authToken,
                                                       DocumentHelper.PaperNotificationRecipient recipient) {
        if (caseDetails.getData().isContestedApplication()) {
            FinremCaseDataContested caseData = (FinremCaseDataContested) caseDetails.getData();
            List<OrderSentToPartiesCollection> sentToPartiesCollection = caseData.getOrdersSentToPartiesCollection();
            List<CaseDocument> consentOrderDocuments = new ArrayList<>();
            sentToPartiesCollection.forEach(doc -> consentOrderDocuments.add(doc.getValue().getCaseDocument()));
            ConsentOrderWrapper wrapper = caseData.getConsentOrderWrapper();
            if (consentOrderApprovedDocumentService.getApprovedOrderModifiedAfterNotApprovedOrder(wrapper, authToken)) {
                consentOrderApprovedDocumentService.addApprovedConsentCoverLetter(caseDetails, consentOrderDocuments, authToken, recipient);
            } else {
                consentOrderNotApprovedDocumentService.addNotApprovedConsentCoverLetter(caseDetails, consentOrderDocuments, authToken, recipient);
            }
            List<BulkPrintDocument> bulkPrintDocumentList = new ArrayList<>();
            consentOrderDocuments.forEach(caseDocument -> bulkPrintDocumentList.add(BulkPrintDocument.builder()
                .fileName(caseDocument.getDocumentFilename()).binaryFileUrl(caseDocument.getDocumentBinaryUrl()).build()));
            return bulkPrintDocumentList;
        }

        return new ArrayList<>();
    }
}