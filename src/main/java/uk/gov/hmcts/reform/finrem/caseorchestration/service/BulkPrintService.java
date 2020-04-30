package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;

@Service
@Slf4j
public class BulkPrintService extends AbstractDocumentService {

    private static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    private final FeatureToggleService featureToggleService;

    @Autowired
    public BulkPrintService(DocumentClient documentClient,
                            DocumentConfiguration config,
                            ObjectMapper objectMapper,
                            FeatureToggleService featureToggleService) {
        super(documentClient, config, objectMapper);
        this.featureToggleService = featureToggleService;
    }

    public UUID sendOrdersForBulkPrint(final CaseDocument coverSheet, final CaseDetails caseDetails) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        log.info("Sending Orders for Bulk Print.");

        bulkPrintDocuments.add(
            BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        List<BulkPrintDocument> approvedOrderCollection = approvedOrderCollection(caseDetails.getData());
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

        return bulkPrint(
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

    List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data) {
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get("approvedOrderCollection"))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (documentList.size() > 0) {
            log.info("Extracting 'approvedOrderCollection' from case data for bulk print.");

            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get(VALUE));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "orderLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, CONSENT_ORDER));

            if (featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()) {
                bulkPrintDocuments.addAll(convertBulkPrintDocument(value, CONSENT_ORDER_APPROVED_NOTIFICATION_LETTER));
                log.info("Approved Consent Order Notification Letter Feature Toggled is Enabled");
                log.info("Adding consentOrderApprovedNotificationLetter document to BulkPrint documents list");
            } else {
                log.info("isApprovedConsentOrderNotificationLetterEnabled toggled off"
                    + " - not sending consentOrderApprovedNotificationLetter to bulk print");
            }

            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "pensionDocuments",
                "uploadedDocument"));
        } else {
            log.info("Failed to extract 'approvedOrderCollection' from case data for bulk print as document list was empty.");
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
