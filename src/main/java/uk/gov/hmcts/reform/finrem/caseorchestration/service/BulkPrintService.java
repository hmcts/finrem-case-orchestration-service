package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintService {

    private static final String FINANCIAL_REMEDY_PACK_LETTER_TYPE = "FINANCIAL_REMEDY_PACK";
    static final String DOCUMENT_FILENAME = "document_filename";
    static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    private final FeatureToggleService featureToggleService;
    private final GenericDocumentService genericDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    public UUID sendNotificationLetterForBulkPrint(final CaseDocument notificationLetter, final CaseDetails caseDetails) {
        List<BulkPrintDocument> notificationLetterList = Collections.singletonList(
            BulkPrintDocument.builder().binaryFileUrl(notificationLetter.getDocumentBinaryUrl()).build());

        Long caseId = caseDetails.getId();
        log.info("Notification letter sent to Bulk Print: {} for Case ID: {}", notificationLetterList, caseId);

        return bulkPrintDocuments(caseId, notificationLetterList);
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
     * Sending approved order collection or upload order (if approved order collection is empty) to bulk print.
     * @param coverSheet cover sheet document
     * @param caseDetails {@link CaseDetails} object
     * @param recipientIsApplicant true if applicant is the recipient, false for respondent being the recipient
     * @return
     */
    private UUID sendOrdersForBulkPrint(final CaseDocument coverSheet, final CaseDetails caseDetails, boolean recipientIsApplicant) {
        log.info("Sending Approved Consent Order to {} / solicitor for Bulk Print", recipientIsApplicant ? "applicant" : "respondent");

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        bulkPrintDocuments.add(BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        if (featureToggleService.isApprovedConsentOrderNotificationLetterEnabled() && recipientIsApplicant) {
            bulkPrintDocuments.addAll(convertBulkPrintDocument(caseDetails.getData(), CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER));
        }

        List<BulkPrintDocument> approvedOrderCollection = approvedOrderCollection(caseDetails.getData());
        bulkPrintDocuments.addAll(approvedOrderCollection);

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
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "orderLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, CONSENT_ORDER));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "pensionDocuments",
                "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
        }

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
            Map value = (Map) document.get(VALUE);

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

    public Optional<UUID> printOrderNotApprovedDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> applicantDocuments = consentOrderNotApprovedDocumentService.generateApplicantDocuments(
            caseDetails, authorisationToken);
        return !applicantDocuments.isEmpty()
            ? Optional.of(bulkPrintDocuments(caseDetails.getId(), applicantDocuments))
            : Optional.empty();
    }

    private UUID bulkPrintDocuments(Long caseId, List<BulkPrintDocument> documents) {
        log.info("Sending {} document(s) to bulk print: {}", documents.size(), documents);

        return genericDocumentService.bulkPrint(
            BulkPrintRequest.builder()
                .caseId(String.valueOf(caseId))
                .letterType(FINANCIAL_REMEDY_PACK_LETTER_TYPE)
                .bulkPrintDocuments(documents)
                .build());
    }
}
