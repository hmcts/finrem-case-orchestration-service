package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class DocumentHandler {

    protected final CaseDocumentCollectionType collectionType;

    private final FeatureToggleService featureToggleService;

    protected abstract List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections);

    protected abstract DocumentCategory getDocumentCategoryFromDocumentType(
        CaseDocumentType caseDocumentType
    );

    public void replaceManagedDocumentsInCollectionType(FinremCallbackRequest callbackRequest,
                                                        List<UploadCaseDocumentCollection> screenCollection) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);

        originalCollectionForType.clear();

        addUploadedDocumentToDocumentCollectionType(callbackRequest, screenCollection);
    }

    public void addUploadedDocumentToDocumentCollectionType(FinremCallbackRequest callbackRequest,
                                                            List<UploadCaseDocumentCollection> screenCollection) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        List<UploadCaseDocumentCollection> originalCollectionForType =
            caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType);
        List<UploadCaseDocumentCollection> uploadedCollectionForType =
            getAlteredCollectionForType(screenCollection);

        applyDocumentCategory(uploadedCollectionForType);

        originalCollectionForType.addAll(uploadedCollectionForType);

        originalCollectionForType.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        log.info("Adding items: {}, to {} Collection", uploadedCollectionForType,
            collectionType);
        screenCollection.removeAll(uploadedCollectionForType);
    }

    private void applyDocumentCategory(List<UploadCaseDocumentCollection> uploadedCollectionForType) {
        if (featureToggleService.isCaseFileViewEnabled()) {
            for (UploadCaseDocumentCollection uploadCaseDocumentCollection : uploadedCollectionForType) {
                CaseDocument caseDocument = uploadCaseDocumentCollection.getUploadCaseDocument()
                    .getCaseDocuments();
                if (caseDocument.getCategoryId() == null) {
                    caseDocument.setCategoryId(getDocumentCategoryFromDocumentType(
                            uploadCaseDocumentCollection.getUploadCaseDocument().getCaseDocumentType()
                        ).getDocumentCategoryId()
                    );
                }
            }
        }
    }

    public void assignDocumentCategoryToUploadDocumentsCollection(FinremCaseData caseData) {
        applyDocumentCategory(caseData.getUploadCaseDocumentWrapper().getDocumentCollectionPerType(collectionType));
    }
}
