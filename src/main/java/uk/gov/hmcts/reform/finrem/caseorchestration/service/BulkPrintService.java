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
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentTranslator.approvedOrderCollection;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentTranslator.uploadOrder;

@Service
@Slf4j
public class BulkPrintService extends AbstractDocumentService {

    @Autowired
    public BulkPrintService(
        DocumentClient documentClient, DocumentConfiguration config, ObjectMapper objectMapper) {
        super(documentClient, config, objectMapper);
    }

    public UUID sendForBulkPrint(final CaseDocument coverSheet, final CaseDetails caseDetails) {

        List<BulkPrintDocument> bulkPrintDocuments = new ArrayList<>();

        bulkPrintDocuments.add(
            BulkPrintDocument.builder().binaryFileUrl(coverSheet.getDocumentBinaryUrl()).build());

        List<BulkPrintDocument> approvedOrderCollection = approvedOrderCollection(caseDetails.getData());
        List<BulkPrintDocument> uploadOrder = uploadOrder(caseDetails.getData());

        if (approvedOrderCollection.size() > 0) {
            bulkPrintDocuments.addAll(approvedOrderCollection);
        } else if (uploadOrder.size() > 0) {
            bulkPrintDocuments.addAll(uploadOrder);
        }

        log.info(" {} Order documents including cover sheet are sent bulk print.", bulkPrintDocuments.size());

        return bulkPrint(
            BulkPrintRequest.builder()
                .caseId(caseDetails.getId().toString())
                .letterType("FINANCIAL_REMEDY_PACK")
                .bulkPrintDocuments(bulkPrintDocuments)
                .build());
    }
}
