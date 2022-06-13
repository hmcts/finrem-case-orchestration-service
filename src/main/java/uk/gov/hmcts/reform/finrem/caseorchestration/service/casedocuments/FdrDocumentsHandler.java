package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;

@Component
@Slf4j
public class FdrDocumentsHandler extends CaseDocumentHandler {

    @Autowired
    public FdrDocumentsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentType() != null
                && d.getUploadedCaseDocument().getCaseDocumentFdr() != null
                && d.getUploadedCaseDocument().getCaseDocumentFdr().equalsIgnoreCase("Yes"))
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
