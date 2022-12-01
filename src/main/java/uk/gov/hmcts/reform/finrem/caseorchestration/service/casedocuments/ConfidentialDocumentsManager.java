package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@Component
@Slf4j
@Order(1)
public class ConfidentialDocumentsManager extends CaseDocumentManager<ConfidentialUploadedDocumentData> {

    @Autowired
    public ConfidentialDocumentsManager(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void manageDocumentCollection(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<UploadCaseDocumentCollection> confidentialFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadCaseDocument().getCaseDocuments() != null
                && d.getUploadCaseDocument().getCaseDocumentType() != null
                && d.getUploadCaseDocument().getCaseDocumentConfidential() != null
                && d.getUploadCaseDocument().getCaseDocumentConfidential().equals(YesOrNo.YES))
            .collect(Collectors.toList());


        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        List<ConfidentialUploadedDocumentData> confidentialDocsCollection = getConfidentialDocumentCollection(
            caseData,CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        if (!confidentialFiltered.isEmpty()) {
            List<ConfidentialUploadedDocumentData> confidentialDocs = confidentialFiltered.stream().map(
                doc -> buildConfidentialDocument(doc)).collect((Collectors.toList()));
            confidentialDocsCollection.addAll(confidentialDocs);
            confidentialDocsCollection.sort(Comparator.comparing(
                ConfidentialUploadedDocumentData::getConfidentialUploadedDocument, Comparator.comparing(
                    ConfidentialUploadedDocument::getConfidentialDocumentUploadDateTime, Comparator.nullsLast(
                        Comparator.reverseOrder()))));
            caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, confidentialDocsCollection);
        }
    }

    private ConfidentialUploadedDocumentData buildConfidentialDocument(UploadCaseDocumentCollection doc) {

        UploadCaseDocument uploadedCaseDocument = doc.getUploadCaseDocument();
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
                .confidentialDocumentUploadDateTime(uploadedCaseDocument.getCaseDocumentUploadDateTime())
                .build()).build();
    }
}
