package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@Order(2)
public class FdrDocumentsHandler extends CaseDocumentHandler<UploadCaseDocumentCollection> {

    @Autowired
    public FdrDocumentsHandler() {

    }

    @Override
    protected List<UploadCaseDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return caseData.getUploadCaseDocumentWrapper().getFdrCaseDocumentCollection();
    }

    @Override
    public void handle(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<UploadCaseDocumentCollection> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getValue();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && uploadedCaseDocument.getCaseDocumentFdr().isYes();
            })
            .collect(Collectors.toList());

        List<UploadCaseDocumentCollection> fdrDocsCollection = getDocumentCollection(caseData);
        fdrDocsCollection.addAll(fdrFiltered);
        log.info("Adding items: {}, to FDR Documents Collection", fdrFiltered);
        uploadedDocuments.removeAll(fdrFiltered);

        if (!fdrDocsCollection.isEmpty()) {
            caseData.getUploadCaseDocumentWrapper().setFdrCaseDocumentCollection(fdrDocsCollection);
        }
    }
}
