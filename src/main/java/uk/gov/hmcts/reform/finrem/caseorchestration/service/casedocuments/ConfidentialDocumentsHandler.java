package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@Component
@Slf4j
public class ConfidentialDocumentsHandler extends CaseDocumentHandler<ConfidentialUploadedDocumentData> {

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


        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        List<ConfidentialUploadedDocumentData> confidentialDocsCollection = getDocumentCollection(caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        if (!confidentialFiltered.isEmpty()) {
            List<ConfidentialUploadedDocumentData> confidentialDocs = confidentialFiltered.stream().map(
                doc -> buildConfidentialDocument(doc)).collect((Collectors.toList()));
            confidentialDocsCollection.addAll(confidentialDocs);
            caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, confidentialDocsCollection);
        }
    }

    private ConfidentialUploadedDocumentData buildConfidentialDocument(ContestedUploadedDocumentData doc) {

        ContestedUploadedDocument uploadedCaseDocument = doc.getUploadedCaseDocument();
        log.info("Build doc with filename {}, and comments {} and document type {}",
            uploadedCaseDocument.getCaseDocuments().getDocumentFilename(),
            uploadedCaseDocument.getHearingDetails(),
            uploadedCaseDocument.getCaseDocumentType());
        return ConfidentialUploadedDocumentData.builder()
            .confidentialUploadedDocument(ConfidentialUploadedDocument.builder()
                .documentFileName(uploadedCaseDocument.getCaseDocuments().getDocumentFilename())
                .documentComment(uploadedCaseDocument.getHearingDetails())
                .documentLink(uploadedCaseDocument.getCaseDocuments())
                .documentType(uploadedCaseDocument.getCaseDocumentType())
                .build()).build();
    }
}
