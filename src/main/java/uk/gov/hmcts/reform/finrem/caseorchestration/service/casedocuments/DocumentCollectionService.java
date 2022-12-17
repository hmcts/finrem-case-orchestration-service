package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class DocumentCollectionService {

    protected final ManageCaseDocumentsCollectionType serviceCollectionType;

    protected abstract List<UploadCaseDocumentCollection> getServiceCollectionType(
        List<UploadCaseDocumentCollection> eventScreenDocumentCollections);

    public void addManagedDocumentToCollection(FinremCallbackRequest callbackRequest,
                                               List<UploadCaseDocumentCollection> allScreenCollections) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalServiceCollection =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollection(serviceCollectionType);
        List<UploadCaseDocumentCollection> screenServiceCollection =
            getServiceCollectionType(allScreenCollections);

        screenServiceCollection.stream()
            .filter(screenServiceDocument -> !originalServiceCollection.contains(screenServiceDocument))
            .forEach(screenServiceDocument -> originalServiceCollection.add(screenServiceDocument));

        originalServiceCollection.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", screenServiceCollection,
            serviceCollectionType);
        allScreenCollections.removeAll(screenServiceCollection);
    }

    public void removeMovedDocumentFromCollection(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<UploadCaseDocumentCollection> originalDocumentCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollection(serviceCollectionType);
        List<UploadCaseDocumentCollection> documentsToRemove = getDocumentsToRemove(caseData);
        documentsToRemove.stream().forEach(originalDocumentCollectionForType::remove);
    }

    private List<UploadCaseDocumentCollection> getDocumentsToRemove(FinremCaseData caseData) {
        List<UploadCaseDocumentCollection> allScreenCollections = caseData.getManageCaseDocumentCollection();
        List<UploadCaseDocumentCollection> serviceScreenCollection = getServiceCollectionType(allScreenCollections);
        List<String> serviceScreenCollectionDocIds = serviceScreenCollection.stream()
            .map(UploadCaseDocumentCollection::getId).collect(Collectors.toList());
        List<UploadCaseDocumentCollection> originalServiceCollection =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollection(serviceCollectionType);

        return originalServiceCollection.stream().filter(originalDoc ->
                !serviceScreenCollectionDocIds.contains(originalDoc.getId()))
            .collect(Collectors.toList());
    }
}
