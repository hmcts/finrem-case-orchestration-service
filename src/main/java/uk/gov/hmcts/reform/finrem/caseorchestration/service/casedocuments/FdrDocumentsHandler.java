package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;

@Component
@Slf4j
@Order(2)
public class FdrDocumentsHandler extends CaseDocumentHandler<ContestedUploadedDocumentData> {

    @Autowired
    public FdrDocumentsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> {
                ContestedUploadedDocument uploadedCaseDocument = d.getUploadedCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && uploadedCaseDocument.getCaseDocumentFdr().equalsIgnoreCase("Yes");
            })
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> fdrDocsCollection = getDocumentCollection(caseData, FDR_DOCS_COLLECTION);
        fdrDocsCollection.addAll(fdrFiltered);
        log.info("Adding items: {}, to FDR Documents Collection", fdrFiltered);
        uploadedDocuments.removeAll(fdrFiltered);


        if (!fdrDocsCollection.isEmpty()) {
            caseData.put(FDR_DOCS_COLLECTION, fdrDocsCollection);
        }

    }
}
