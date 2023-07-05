package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class DocumentHandler {

    protected final ManageCaseDocumentsCollectionType collectionType;

    protected abstract List<UploadCaseDocumentCollection> getTypedManagedDocumentCollections(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections);

    public void addManagedDocumentToSelectedCollection(FinremCallbackRequest callbackRequest,
                                                       List<UploadCaseDocumentCollection> allManagedDocumentCollections) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> typedOriginalDocumentCollections =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);
        List<UploadCaseDocumentCollection> typedManagedDocumentCollections =
            getTypedManagedDocumentCollections(allManagedDocumentCollections);

        typedManagedDocumentCollections.stream()
            .filter(managedDocumentCollection -> !typedOriginalDocumentCollections.contains(managedDocumentCollection))
            .forEach(typedOriginalDocumentCollections::add);

        typedOriginalDocumentCollections.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", typedManagedDocumentCollections,
            collectionType);
        allManagedDocumentCollections.removeAll(typedManagedDocumentCollections);
    }

    public void removeManagedDocumentFromOriginalCollection(FinremCallbackRequest callbackRequest) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<UploadCaseDocumentCollection> originalDocumentCollection =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);
        List<UploadCaseDocumentCollection> documentsToBeRemoved =
            getDocumentsToBeRemovedFromOriginalCollection(caseData);
        documentsToBeRemoved.forEach(originalDocumentCollection::remove);
    }

    protected boolean isIntervener(CaseDocumentParty caseDocumentParty) {
        return caseDocumentParty != null
            && (caseDocumentParty.equals(CaseDocumentParty.INTERVENER_ONE)
            || caseDocumentParty.equals(CaseDocumentParty.INTERVENER_TWO)
            || caseDocumentParty.equals(CaseDocumentParty.INTERVENER_THREE)
            || caseDocumentParty.equals(CaseDocumentParty.INTERVENER_FOUR));
    }

    private List<UploadCaseDocumentCollection> getDocumentsToBeRemovedFromOriginalCollection(FinremCaseData caseData) {
        List<UploadCaseDocumentCollection> managedCollections = caseData.getManageCaseDocumentCollection();
        List<UploadCaseDocumentCollection> managedCollection = getTypedManagedDocumentCollections(managedCollections);
        List<String> managedCollectionDocIds = managedCollection.stream()
            .map(UploadCaseDocumentCollection::getId).toList();
        List<UploadCaseDocumentCollection> originalDocumentCollection =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);

        return originalDocumentCollection.stream().filter(originalDoc ->
                !managedCollectionDocIds.contains(originalDoc.getId()))
            .collect(Collectors.toList());
    }
}
