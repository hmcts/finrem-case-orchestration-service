package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getValue;

@Slf4j
public final class BulkPrintDocumentTranslator {

    public static final String DOCUMENT_FILENAME = "document_filename";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    private BulkPrintDocumentTranslator() {

    }

    public static List<BulkPrintDocument> convertDocument(CaseDetails caseDetails, String collectionName,
                                                          String documentName) {
        log.info("Extracting {} from case data  for bulk print. ", collectionName);
        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();
        List<Map> documentList =
            ofNullable(caseDetails.getData().get("pensionCollection"))
                .map(i -> (List<Map>) i)
                .orElse(new ArrayList<>());

        for (Map<String, Object> document : documentList) {

            Map<String, Object> value = ((Map) document.get(VALUE));

            Optional<Object> documentLinkObj = getValue.apply(value, "uploadedDocument");

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

    public static List<BulkPrintDocument> convertDocument(CaseDetails caseDetails, String documentName) {
        log.info("Extracting {} from case data  for bulk print. ", documentName);

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        Optional<Object> documentLinkObj = getValue.apply(caseDetails.getData(), documentName);

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


}
