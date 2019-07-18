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
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getValue;

@Service
@Slf4j
public class BulkPrintService extends AbstractDocumentService {
    public static final String UPLOAD_ORDER = "uploadOrder";
    private static final String DOCUMENT_LINK = "DocumentLink";
    private static final String DOCUMENT_URL = "document_binary_url";
    private static final String VALUE = "value";

    @Autowired
    public BulkPrintService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public UUID sendForBulkPrint(final CaseDocument coverSheet, final CaseDetails caseDetails) {

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        bulkPrintDocuments.add(
            BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        log.info("extracting order documents from case data  for bulk print. ");
        List<Map> documentList =
            ofNullable(caseDetails.getData().get(UPLOAD_ORDER))
                .map(i -> (List<Map>) i)
                .orElse(new ArrayList<>());

        for (Map<String, Object> document : documentList) {

            Map<String, Object> value = ((Map) document.get(VALUE));

            Optional<Object> documentLinkObj = getValue.apply(value, DOCUMENT_LINK);

            if (documentLinkObj.isPresent()) {
                Map<String, Object> documentLink = (Map) documentLinkObj.get();
                bulkPrintDocuments.add(
                    BulkPrintDocument.builder()
                        .binaryFileUrl(documentLink.get(DOCUMENT_URL).toString())
                        .build());
            }
        }
        log.info(
            " {} order documents with cover sheet are sent bulk print.", bulkPrintDocuments.size());

        return bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build());
    }


}
