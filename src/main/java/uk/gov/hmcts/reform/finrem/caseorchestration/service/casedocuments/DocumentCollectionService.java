package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class DocumentCollectionService {

    protected final ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType;
    protected final EvidenceManagementDeleteService evidenceManagementDeleteService;

    protected abstract List<UploadCaseDocumentCollection> getDocumentForCollectionServiceType(
        List<UploadCaseDocumentCollection> eventScreenDocumentCollections);

    public void processUploadDocumentCollection(FinremCallbackRequest callbackRequest,
                                                List<UploadCaseDocumentCollection> allManagedDocumentCollections) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalDocumentCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollection(manageCaseDocumentsCollectionType);

        List<UploadCaseDocumentCollection> managedDocumentCollectionForType =
            getDocumentForCollectionServiceType(allManagedDocumentCollections);

        originalDocumentCollectionForType.addAll(managedDocumentCollectionForType);
        originalDocumentCollectionForType.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", managedDocumentCollectionForType,
            manageCaseDocumentsCollectionType);
        allManagedDocumentCollections.removeAll(managedDocumentCollectionForType);
    }

    public void deleteEventRemovedDocuments(FinremCallbackRequest callbackRequest, String authToken) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBeforeEvent = callbackRequest.getCaseDetailsBefore().getData();
        List<UploadCaseDocumentCollection> originalDocumentCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollection(manageCaseDocumentsCollectionType);
        List<UploadCaseDocumentCollection> documentsForDeletion =
            getDocumentsForDeletion(caseData, caseDataBeforeEvent);
        List<UploadCaseDocumentCollection> documentsForDeletionForCollectionType =
            getDocumentForCollectionServiceType(documentsForDeletion);
        documentsForDeletionForCollectionType.stream()
            .forEach(documentForDeletion -> {
                originalDocumentCollectionForType.remove(documentForDeletion);
                evidenceManagementDeleteService.deleteFile(
                    documentForDeletion.getUploadCaseDocument().getCaseDocuments().getDocumentUrl(), authToken);
            });
    }

    private List<UploadCaseDocumentCollection> getDocumentsForDeletion(FinremCaseData caseData,
                                                                       FinremCaseData caseDataBeforeEvent) {
        List<String> documentIdsAfterEvent =
            caseData.getUploadCaseDocumentWrapper().getAllCollections().stream()
                .map(UploadCaseDocumentCollection::getId).collect(Collectors.toList());

        return caseDataBeforeEvent.getUploadCaseDocumentWrapper().getAllCollections().stream()
            .filter(documentCollectionBeforeEvent ->
                !documentIdsAfterEvent.contains(documentCollectionBeforeEvent.getId())).collect(Collectors.toList());
    }
}
