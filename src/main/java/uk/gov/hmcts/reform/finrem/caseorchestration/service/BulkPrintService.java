package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isOrderNotApprovedDocumentCollectionPresent;

@Service
@Slf4j
public class BulkPrintService {

    private static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    static final String DOCUMENT_FILENAME = "document_filename";

    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final DocumentHelper documentHelper;

    public BulkPrintService(GenericDocumentService genericDocumentService,
                            ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService,
                            @Lazy ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                            DocumentHelper documentHelper) {
        this.genericDocumentService = genericDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.documentHelper = documentHelper;
    }


    public UUID sendNotificationLetterForBulkPrint(final CaseDocument notificationLetter, final CaseDetails caseDetails) {
        List<BulkPrintDocument> notificationLetterList = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(notificationLetter.getDocumentBinaryUrl()).build());

        Long caseId = caseDetails.getId();
        log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", notificationLetterList, caseId);

        return bulkPrintDocuments(caseId, notificationLetterList);
    }

    public UUID sendOrderForBulkPrintRespondent(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        log.info("Sending order documents to recipient / solicitor for Bulk Print");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> orderDocuments = isOrderNotApprovedDocumentCollectionPresent(caseData)
            ? asList(consentOrderNotApprovedDocumentService.generalOrder(caseData))
            : approvedOrderCollection(caseData);

        bulkPrintDocuments.addAll(orderDocuments);

        return bulkPrintDocuments(caseDetails.getId(), bulkPrintDocuments);
    }

    List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get(APPROVED_ORDER_COLLECTION))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (!documentList.isEmpty()) {
            log.info("Extracting 'approvedOrderCollection' from case data for bulk print.");
            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get(VALUE));
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, "orderLetter").ifPresent(bulkPrintDocuments::add);
            documentHelper.getDocumentLinkAsBulkPrintDocument(value, CONSENT_ORDER).ifPresent(bulkPrintDocuments::add);
            bulkPrintDocuments.addAll(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(value,
                "pensionDocuments", "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
        }

        return bulkPrintDocuments;
    }

    public UUID printApplicantConsentOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintDocuments(caseDetails.getId(), applicantDocuments);
    }

    public UUID printApplicantConsentOrderApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderApprovedDocumentService.prepareApplicantLetterPack(
            caseDetails, authorisationToken);
        return bulkPrintDocuments(caseDetails.getId(), applicantDocuments);
    }

    private UUID bulkPrintDocuments(Long caseId, List<BulkPrintDocument> documents) {
        UUID letterId = genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(FINANCIAL_REMEDY_PACK_LETTER_TYPE)
                .bulkPrintDocuments(documents)
                .build());

        log.info("Letter ID {} for {} document(s) sent to bulk print: {}", letterId, documents.size(), documents);

        return letterId;
    }
}
