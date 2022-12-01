package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FDR_DOCS_COLLECTION;

@Component
@Slf4j
@Order(2)
public class FdrDocumentsManager extends CaseDocumentManager<UploadCaseDocumentCollection> {

    @Override
    public void manageDocumentCollection(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<UploadCaseDocumentCollection> fdrFiltered = uploadedDocuments.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && uploadedCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
            })
            .collect(Collectors.toList());

        List<UploadCaseDocumentCollection> fdrDocsCollection = caseData.getUploadCaseDocumentWrapper().getFdrCaseDocumentCollection();
        fdrDocsCollection.addAll(fdrFiltered);
        fdrDocsCollection.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to FDR Documents Collection", fdrFiltered);
        uploadedDocuments.removeAll(fdrFiltered);
    }
}
