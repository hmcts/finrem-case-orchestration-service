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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_DOCUMENTS_COLLECTION;

@Component
@Slf4j
@Order(1)
public class GeneralDocumentsHandler extends CaseDocumentHandler<ContestedUploadedDocumentData> {

    @Autowired
    public GeneralDocumentsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void handle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> generalFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() == null)
            .collect(Collectors.toList());


        log.info("Adding items: {}, to General Documents Collection", generalFiltered);
        uploadedDocuments.removeAll(generalFiltered);

        List<ContestedUploadedDocumentData> generalDocsCollection = getDocumentCollection(caseData, GENERAL_DOCUMENTS_COLLECTION);
        if (!generalFiltered.isEmpty()) {
            List<ContestedUploadedDocumentData> generalDocs = generalFiltered.stream().map(
                this::buildGeneralDocument).collect((Collectors.toList()));
            generalDocsCollection.addAll(generalDocs);
            caseData.put(GENERAL_DOCUMENTS_COLLECTION, generalDocsCollection);
        }
    }

    private ContestedUploadedDocumentData buildGeneralDocument(ContestedUploadedDocumentData doc) {

        ContestedUploadedDocument uploadedCaseDocument = doc.getUploadedCaseDocument();
        log.info("Build doc with filename {}, and comments {} and document type {}",
            uploadedCaseDocument.getCaseDocuments().getDocumentFilename(),
            uploadedCaseDocument.getHearingDetails(),
            uploadedCaseDocument.getCaseDocumentType());
        return ContestedUploadedDocumentData.builder()
            .uploadedCaseDocument(ContestedUploadedDocument.builder()
                .documentFileName(uploadedCaseDocument.getCaseDocuments().getDocumentFilename())
                .documentComment(uploadedCaseDocument.getDocumentComment())
                .caseDocuments(uploadedCaseDocument.getCaseDocuments())
                .caseDocumentType(uploadedCaseDocument.getCaseDocumentType())
                .build()).build();
    }
}
