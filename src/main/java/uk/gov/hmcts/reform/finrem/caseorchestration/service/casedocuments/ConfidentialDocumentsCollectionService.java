package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Order(1)
public class ConfidentialDocumentsCollectionService extends DocumentCollectionService {

    public ConfidentialDocumentsCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(null, evidenceManagementDeleteService);
    }

    @Override
    public void addManagedDocumentToCollection(FinremCallbackRequest callbackRequest,
                                               List<UploadCaseDocumentCollection> allScreenCollections) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> managedDocumentCollectionForType =
            getServiceCollectionType(allScreenCollections);

        log.info("Adding items: {}, to Confidential Documents Collection", managedDocumentCollectionForType);
        allScreenCollections.removeAll(managedDocumentCollectionForType);

        List<UploadConfidentialDocumentCollection> confidentialDocsCollection = caseData.getConfidentialDocumentsUploaded();

        if (!managedDocumentCollectionForType.isEmpty()) {
            List<UploadConfidentialDocumentCollection> confidentialDocs = managedDocumentCollectionForType.stream().map(
                doc -> buildConfidentialDocument(doc)).collect((Collectors.toList()));
            confidentialDocsCollection.addAll(confidentialDocs);

            confidentialDocsCollection.sort(Comparator.comparing(
                UploadConfidentialDocumentCollection::getValue, Comparator.comparing(
                    UploadConfidentialDocument::getConfidentialDocumentUploadDateTime, Comparator.nullsLast(
                        Comparator.reverseOrder()))));
        }
        allScreenCollections.removeAll(managedDocumentCollectionForType);

    }

    protected List<UploadCaseDocumentCollection> getServiceCollectionType(
        List<UploadCaseDocumentCollection> eventScreenDocumentCollections) {

        return eventScreenDocumentCollections.stream()
            .filter(d -> d.getUploadCaseDocument().getCaseDocuments() != null
                && d.getUploadCaseDocument().getCaseDocumentType() != null
                && d.getUploadCaseDocument().getCaseDocumentConfidential() != null
                && d.getUploadCaseDocument().getCaseDocumentConfidential().equals(YesOrNo.YES))
            .collect(Collectors.toList());
    }

    private UploadConfidentialDocumentCollection buildConfidentialDocument(UploadCaseDocumentCollection doc) {

        UploadCaseDocument uploadedCaseDocument = doc.getUploadCaseDocument();
        log.info("Build doc with filename {}, and comments {} and document type {}",
            uploadedCaseDocument.getCaseDocuments().getDocumentFilename(),
            uploadedCaseDocument.getHearingDetails(),
            uploadedCaseDocument.getCaseDocumentType());
        return UploadConfidentialDocumentCollection.builder()
            .value(UploadConfidentialDocument.builder()
                .documentFileName(uploadedCaseDocument.getCaseDocuments().getDocumentFilename())
                .documentComment(uploadedCaseDocument.getHearingDetails())
                .documentLink(uploadedCaseDocument.getCaseDocuments())
                .documentType(uploadedCaseDocument.getCaseDocumentType())
                .confidentialDocumentUploadDateTime(uploadedCaseDocument.getCaseDocumentUploadDateTime())
                .build()).build();
    }
}
