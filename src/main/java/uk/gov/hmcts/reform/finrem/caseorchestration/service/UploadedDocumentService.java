package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UploadedDocumentService extends DocumentDateService<UploadCaseDocumentCollection> {

    private final EvidenceManagementDeleteService evidenceManagementDeleteService;

    public UploadedDocumentService(ObjectMapper objectMapper,
                                   EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(objectMapper, UploadCaseDocumentCollection.class);
        this.evidenceManagementDeleteService = evidenceManagementDeleteService;
    }

    public void deleteRemovedDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore, String auth) {
        List<UploadCaseDocumentCollection> allCollectionsNow =
            caseData.getUploadCaseDocumentWrapper().getAllCollections();
        List<UploadCaseDocumentCollection> allCollectionsBefore =
            caseDataBefore.getUploadCaseDocumentWrapper().getAllCollections();

        allCollectionsBefore.stream()
            .filter(documentCollectionBefore ->
                !getDocumentCollectionIds(allCollectionsNow).contains(documentCollectionBefore.getId()))
            .forEach(documentCollectionBefore ->
                evidenceManagementDeleteService.deleteFile(documentCollectionBefore.getUploadCaseDocument()
                    .getCaseDocuments().getDocumentUrl(), auth));
    }

    private static List<String> getDocumentCollectionIds(List<UploadCaseDocumentCollection> documentCollections) {
        return documentCollections.stream().map(UploadCaseDocumentCollection::getId).collect(Collectors.toList());
    }
}
