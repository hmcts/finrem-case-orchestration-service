package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getValue;

@Slf4j
public final class BulkPrintDocumentTranslator {

    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    private BulkPrintDocumentTranslator() {

    }

    public static List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data) {
        log.info("Extracting {} from case data  for bulk print. ", "approvedOrderCollection");
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get("approvedOrderCollection"))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (documentList.size() > 0) {
            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get("value"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "orderLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "consentOrder"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "pensionDocuments",
                "uploadedDocument"));
        }

        return bulkPrintDocuments;
    }

    public static List<BulkPrintDocument> uploadOrder(Map<String, Object> data) {
        log.info("Extracting {} from case data  for bulk print. ", "uploadOrder");
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get("uploadOrder"))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());
        if (documentList.size() > 0) {
            Map<String, Object> value = ((Map) getLastMapValue.apply(documentList).get("value"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "DocumentLink"));
        }
        return bulkPrintDocuments;
    }

    private static List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String documentName) {
        log.info("Extracting {} from case data  for bulk print. ", documentName);

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        Optional<Object> documentLinkObj = getValue.apply(data, documentName);

        if (documentLinkObj.isPresent()) {
            Map<String, Object> documentLink = (Map) documentLinkObj.get();
            bulkPrintDocuments.add(
                BulkPrintDocument.builder()
                    .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                    .build());
            log.info("{} for bulk print {}", documentName, documentLink.get(DOCUMENT_FILENAME));
        }
        return bulkPrintDocuments;
    }


    private static List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String collectionName,
                                                                    String documentName) {
        log.info("Extracting {} from case data  for bulk print. ", collectionName);
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        List<Map> documentList = ofNullable(data.get(collectionName))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        for (Map<String, Object> document : documentList) {

            Map<String, Object> value = ((Map) document.get(VALUE));

            Optional<Object> documentLinkObj = getValue.apply(value, documentName);

            if (documentLinkObj.isPresent()) {
                Map<String, Object> documentLink = (Map) documentLinkObj.get();
                bulkPrintDocuments.add(
                    BulkPrintDocument.builder()
                        .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                        .build());
                log.info("{} file for bulk print {}", collectionName, documentLink.get(DOCUMENT_FILENAME));
            }
        }
        return bulkPrintDocuments;
    }

}
