package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadConfidentialDocumentCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.isYes;

@Component
@Slf4j
@Order(1)
public class ConfidentialDocumentsHandler extends CaseDocumentHandler<UploadConfidentialDocumentCollection> {

    @Autowired
    public ConfidentialDocumentsHandler() {
    }

    @Override
    protected List<UploadConfidentialDocumentCollection> getDocumentCollection(FinremCaseData caseData) {
        return caseData.getConfidentialDocumentsUploaded();
    }

    @Override
    public void handle(List<UploadCaseDocumentCollection> uploadedDocuments, FinremCaseData caseData) {
        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<UploadCaseDocumentCollection> confidentialFiltered = uploadedDocuments.stream()
            .filter(d -> d.getValue().getCaseDocuments() != null
                && d.getValue().getCaseDocumentType() != null
                && d.getValue().getCaseDocumentConfidential() != null
                && isYes(d.getValue().getCaseDocumentConfidential()))
            .collect(Collectors.toList());


        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        List<UploadConfidentialDocumentCollection> confidentialDocsCollection =
            Optional.ofNullable(caseData.getConfidentialDocumentsUploaded()).orElse(new ArrayList<>());

        if (!confidentialFiltered.isEmpty()) {
            List<UploadConfidentialDocumentCollection> confidentialDocs = confidentialFiltered.stream().map(
                this::buildConfidentialDocument).collect((Collectors.toList()));
            confidentialDocsCollection.addAll(confidentialDocs);
            caseData.setConfidentialDocumentsUploaded(confidentialDocsCollection);
        }
    }

    private UploadConfidentialDocumentCollection buildConfidentialDocument(UploadCaseDocumentCollection doc) {
        UploadCaseDocument uploadedCaseDocument = doc.getValue();
        log.info("Build doc with filename {}, and comments {} and document type {}",
            uploadedCaseDocument.getCaseDocuments().getFilename(),
            uploadedCaseDocument.getHearingDetails(),
            uploadedCaseDocument.getCaseDocumentType());

        return UploadConfidentialDocumentCollection.builder()
            .value(UploadConfidentialDocument.builder()
                .documentFileName(uploadedCaseDocument.getCaseDocuments().getFilename())
                .documentComment(uploadedCaseDocument.getHearingDetails())
                .documentLink(uploadedCaseDocument.getCaseDocuments())
                .documentType(uploadedCaseDocument.getCaseDocumentType())
                .build()).build();
    }
}
