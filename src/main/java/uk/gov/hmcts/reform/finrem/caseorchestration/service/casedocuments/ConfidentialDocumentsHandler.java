package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@Component
@Slf4j
public class ConfidentialDocumentsHandler extends CaseDocumentHandler {

    @Autowired
    public ConfidentialDocumentsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> confidentialFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentType() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential().equalsIgnoreCase("Yes"))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> confidentialDocsCollection = getDocumentCollection(caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        confidentialDocsCollection.addAll(confidentialFiltered);
        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        if (!confidentialDocsCollection.isEmpty()) {
            List<ConfidentialUploadedDocumentData> confidentialDocs = confidentialDocsCollection.stream().map(
                doc -> ConfidentialUploadedDocumentData.builder()
                    .confidentialUploadedDocument(ConfidentialUploadedDocument.builder()
                        .documentFileName(doc.getUploadedCaseDocument().getCaseDocuments().getDocumentFilename())
                        .documentComment(doc.getUploadedCaseDocument().getHearingDetails())
                        .documentLink(doc.getUploadedCaseDocument().getCaseDocuments())
                        .build()).build()).collect((Collectors.toList()));
            caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, confidentialDocs);
        }
    }
}
