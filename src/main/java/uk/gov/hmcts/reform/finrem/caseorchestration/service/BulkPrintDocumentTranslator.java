package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.DOCUMENT_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getFirstMapValue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getLastMapValue;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BulkPrintDocumentTranslator {

    public static List<BulkPrintDocument> approvedOrderCollection(Map<String, Object> data) {
        log.info("Extracting 'approvedOrderCollection' from case data for bulk print.");
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get("approvedOrderCollection"))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        if (documentList.size() > 0) {
            Map<String, Object> value = ((Map) getFirstMapValue.apply(documentList).get(VALUE));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "consentOrderApprovedLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "orderLetter"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "consentOrder"));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "pensionDocuments", "uploadedDocument"));
        }

        return bulkPrintDocuments;
    }

    public static List<BulkPrintDocument> uploadOrder(Map<String, Object> data) {
        log.info("Extracting 'uploadOrder' from case data for bulk print.");
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList = ofNullable(data.get(UPLOAD_ORDER))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());
        if (documentList.size() > 0) {
            Map value = ((Map) getLastMapValue.apply(documentList).get(VALUE));
            bulkPrintDocuments.addAll(convertBulkPrintDocument(value, "DocumentLink"));
        }
        return bulkPrintDocuments;
    }

    private static List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String documentName) {
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

    private static List<BulkPrintDocument> convertBulkPrintDocument(Map<String, Object> data, String collectionName,
                                                                    String documentName) {
        log.info("Extracting '{}' collection from case data for bulk print.", collectionName);
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        List<Map> documentList = ofNullable(data.get(collectionName))
            .map(i -> (List<Map>) i)
            .orElse(new ArrayList<>());

        for (Map document : documentList) {
            Map value = ((Map) document.get(VALUE));

            Object documentLinkObj = value.get(documentName);

            if (documentLinkObj != null) {
                Map documentLink = (Map) documentLinkObj;
                bulkPrintDocuments.add(BulkPrintDocument.builder()
                    .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                    .build());
                log.info("Sending {} ({}) for bulk print.", collectionName, documentLink.get(DOCUMENT_FILENAME));
            }
        }
        return bulkPrintDocuments;
    }
}
