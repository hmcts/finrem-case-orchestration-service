package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class DocumentHandler {

    protected final CaseDocumentCollectionType collectionType;

    protected abstract List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections);

    public void replaceManagedDocumentsInCollectionType(FinremCallbackRequest<FinremCaseDataContested> callbackRequest,
                                                        List<UploadCaseDocumentCollection> screenCollection) {
        FinremCaseDataContested caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);

        originalCollectionForType.clear();

        addUploadedDocumentToDocumentCollectionType(callbackRequest, screenCollection);
    }

    public void addUploadedDocumentToDocumentCollectionType(FinremCallbackRequest<FinremCaseDataContested> callbackRequest,
                                                            List<UploadCaseDocumentCollection> screenCollection) {
        FinremCaseDataContested caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);
        List<UploadCaseDocumentCollection> uploadedCollectionForType =
            getAlteredCollectionForType(screenCollection);

        originalCollectionForType.addAll(uploadedCollectionForType);

        originalCollectionForType.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", uploadedCollectionForType,
            collectionType);
        screenCollection.removeAll(uploadedCollectionForType);
    }
}
