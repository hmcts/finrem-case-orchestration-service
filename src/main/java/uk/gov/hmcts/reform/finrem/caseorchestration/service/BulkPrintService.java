package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintService {

    private static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    private final FeatureToggleService featureToggleService;
    private final GenericDocumentService genericDocumentService;

    public UUID sendNotificationLetterForBulkPrint(final CaseDocument notificationLetter, final CaseDetails caseDetails) {
        List<BulkPrintDocument> notificationLetterList = new ArrayList<>();
        log.info("Sending Notification Letter for Bulk Print.");

        String caseId = caseDetails.getId().toString();

        notificationLetterList.add(
            BulkPrintDocument.builder().binaryFileUrl(notificationLetter.getDocumentBinaryUrl()).build());

        log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", notificationLetterList, caseId);

        return genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseId)
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(notificationLetterList)
                .build());
    }

    /**
     * Send order documents to printing and posting to applicant or applicant solicitor.
     */
    public UUID sendOrderForBulkPrintApplicant(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        return sendOrdersForBulkPrint(coverSheet, caseDetails, true);
    }

    /**
     * Send order documents to printing and posting to respondent or respondent solicitor.
     */
    public UUID sendOrderForBulkPrintRespondent(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        return sendOrdersForBulkPrint(coverSheet, caseDetails, false);
    }

    /**
     * @param coverSheet cover sheet document
     * @param caseDetails
     * @param recipientIsApplicant true if applicant is the recipient, false for respondent being the recipient.
     * @return
     */
    private UUID sendOrdersForBulkPrint(final CaseDocument coverSheet, final CaseDetails caseDetails, boolean recipientIsApplicant) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        log.info("Sending Orders for Bulk Print.");

        bulkPrintDocuments.add(
            BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        List<BulkPrintDocument> approvedOrderCollection = approvedOrderCollection(caseDetails.getData(), recipientIsApplicant);
        List<BulkPrintDocument> uploadOrder = uploadOrder(caseDetails.getData());

        if (!approvedOrderCollection.isEmpty()) {
            log.info("Sending Approved Order Collections for Bulk Print.: {}", approvedOrderCollection);

            bulkPrintDocuments.addAll(approvedOrderCollection);
        } else if (!uploadOrder.isEmpty()) {
            log.info("Sending Upload Order Collections for Bulk Print: {}", uploadOrder);
            bulkPrintDocuments.addAll(uploadOrder);
        }

        log.info("{} Order documents (including cover sheet) have been sent to bulk print.", bulkPrintDocuments.size());
        log.info("Documents sent to Bulk Print: {}", bulkPrintDocuments);

        return genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build());
    }

    List<BulkPrintDocument> uploadOrder(Map<String, Object> data) {
        log.info("Extracting 'uploadOrder' from case data for bulk print.");
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get(UPLOAD_ORDER))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());
        if (!documentList.isEmpty()) {
            Map<String, Object> value = ((Map) getLastMapValue.apply(documentList).get(VALUE));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "DocumentLink"));
        }
        return bulkPrintDocuments;
    }

    List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data, boolean recipientIsApplicant) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get(APPROVED_ORDER_COLLECTION))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (documentList.size() > 0) {
            log.info("Extracting 'approvedOrderCollection' from case data for bulk print.");

            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get(VALUE));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "orderLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, CONSENT_ORDER));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "pensionDocuments",
                "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
        }

        if (featureToggleService.isApprovedConsentOrderNotificationLetterEnabled() && recipientIsApplicant) {
            log.info("Adding consentOrderApprovedNotificationLetter document to BulkPrint documents list");
            bulkPrintDocuments.addAll(convertBulkPrintDocument(data, CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER));
        }

        log.info("Documents inside 'approvedOrderCollection' are: {}", bulkPrintDocuments);
        return bulkPrintDocuments;
    }

    private List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String documentName) {
        log.info("Extracting '{}' document from case data for bulk print.", documentName);
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        Object documentLinkObj = data.get(documentName);

        if (documentLinkObj != null) {
            Map documentLink = (Map) documentLinkObj;
            bulkPrintDocuments.add(BulkPrintDocument.builder()
                .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                .build());
            log.info("Sending {} ({}) for bulk print.", documentName, documentLink.get(DOCUMENT_FILENAME));
        }
        return bulkPrintDocuments;
    }

    private List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String collectionName, String documentName) {
        log.info("Extracting '{}' collection from case data for bulk print.", collectionName);

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        List<Map> documentList = ofNullable(data.get(collectionName))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        for (Map document : documentList) {
            Map value = ((Map) document.get(VALUE));

            Object documentLinkObj = value.get(documentName);

            if (documentLinkObj != null) {
                Map<String, Object> documentLink = (Map) documentLinkObj;
                bulkPrintDocuments.add(BulkPrintDocument.builder()
                    .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                    .build());
                log.info("Sending {} ({}) for bulk print.", collectionName, documentLink.get(DOCUMENT_FILENAME));
            }
        }
        return bulkPrintDocuments;
    }
}
