package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandler;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.UploadCaseDocumentWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseDocumentsService {

    private final List<CaseDocumentHandler<UploadCaseDocumentCollection>> caseDocumentHandlers;
    private final List<Field> contestedUploadCaseFilesCollectionFields = Arrays.stream(ContestedUploadCaseFilesCollectionType.values())
        .map(ContestedUploadCaseFilesCollectionType::getManageCaseDocumentCollectionField)
        .collect(Collectors.toList());

    public FinremCaseData setApplicantAndRespondentDocumentsCollection(FinremCaseDetails caseDetails) {
        try {
            Map<String, Field> idToCollectionData = findCollectionNameOfDocument(caseDetails.getCaseData());
            idToCollectionData.forEach((key, value) -> value.setAccessible(true));
            findAndRemoveMovedDocumentFromCollections(caseDetails.getCaseData(), idToCollectionData);

            caseDetails.getCaseData().getUploadCaseDocumentWrapper()
                .setManageCaseDocumentCollection(getAllContestedUploadDocuments(caseDetails.getCaseData()));
            return caseDetails.getCaseData();
        } catch (IllegalAccessException e) {
            log.error("Illegal access has occurred {}, : message: {}", e.getCause(), e.getMessage());
            throw new IllegalStateException("Illegal access has occurred, could not manage case documents");
        }
    }

    public FinremCaseData manageCaseDocuments(FinremCaseData caseData) {
        try {
            removeDeletedFilesFromCaseData(caseData);

            Map<String, Field> idToCollectionData = findCollectionNameOfDocument(caseData);
            findAndRemoveMovedDocumentFromCollections(caseData, idToCollectionData);

            List<UploadCaseDocumentCollection> caseDocuments = caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection();
            caseDocumentHandlers.forEach(h -> h.handle(caseDocuments, caseData));

            caseData.getUploadCaseDocumentWrapper().setUploadCaseDocument(caseDocuments);

            return caseData;
        } catch (IllegalAccessException e) {
            log.error("Illegal access has occurred {}, : message: {}", e.getCause(), e.getMessage());
            throw new IllegalStateException("Illegal access has occurred, could not manage case documents");
        }
    }

    private void removeDeletedFilesFromCaseData(FinremCaseData caseData) {

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection().stream()
                .map(UploadCaseDocumentCollection::getId)
                .map(UUID::toString).collect(Collectors.toSet());

        contestedUploadCaseFilesCollectionFields.forEach(collection -> {
            collection.setAccessible(true);
            performDeleteOperation(caseData, findRemainingApplicantAndRespondentDocumentIds, collection);
        });
    }

    private void performDeleteOperation(FinremCaseData caseData,
                                        Set<String> findRemainingApplicantAndRespondentDocumentIds,
                                        Field collection) {
        try {
            Optional.ofNullable(collection.get(caseData.getUploadCaseDocumentWrapper()))
                .ifPresent(value -> replaceCollectionWithValue(caseData, findRemainingApplicantAndRespondentDocumentIds,
                    collection, (List<UploadCaseDocumentCollection>) value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Illegal access has occurred, could not manage case documents");
        }
    }

    private void replaceCollectionWithValue(FinremCaseData caseData, Set<String> findRemainingApplicantAndRespondentDocumentIds,
                                            Field collection, List<UploadCaseDocumentCollection> value) {
        try {
            collection.set(caseData.getUploadCaseDocumentWrapper(),
                filterRemainingDocuments(findRemainingApplicantAndRespondentDocumentIds, value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Illegal access has occurred, could not manage case documents");
        }
    }

    private List<UploadCaseDocumentCollection> filterRemainingDocuments(Set<String> findRemainingApplicantAndRespondentDocumentIds,
                                                                        List<UploadCaseDocumentCollection> value) {
        return value.stream()
            .filter(contestedUploadedDocumentData -> findRemainingApplicantAndRespondentDocumentIds.contains(
                contestedUploadedDocumentData.getId().toString()))
            .collect(Collectors.toList());
    }

    private List<UploadCaseDocumentCollection> getAllContestedUploadDocuments(FinremCaseData caseData) throws IllegalAccessException {

        List<UploadCaseDocumentCollection> allDocuments = new ArrayList<>();
        UploadCaseDocumentWrapper uploadCaseDocumentData = caseData.getUploadCaseDocumentWrapper();

        for (Field collectionType : contestedUploadCaseFilesCollectionFields) {
            collectionType.setAccessible(true);
            Optional.ofNullable(collectionType.get(uploadCaseDocumentData))
                .ifPresent(value -> allDocuments.addAll((List<UploadCaseDocumentCollection>) value));
        }

        return allDocuments;
    }

    private void findAndRemoveMovedDocumentFromCollections(FinremCaseData caseData, Map<String, Field> documentIdsAndCollection) {

        List<UploadCaseDocumentCollection> caseDocumentsCollection = getManageDocumentCollection(caseData);
        UploadCaseDocumentWrapper uploadCaseDocumentData = caseData.getUploadCaseDocumentWrapper();

        documentIdsAndCollection.forEach((documentId, collection) -> {
            try {
                List<UploadCaseDocumentCollection> allDocumentsInCollection =
                    getAllDocumentsInCollection(uploadCaseDocumentData, collection);

                allDocumentsInCollection.forEach(document -> {
                    try {
                        collection.setAccessible(true);
                        collection.set(uploadCaseDocumentData, findIfDocumentExistInCollectionAfterMove(
                            caseData, allDocumentsInCollection, document, caseDocumentsCollection));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Illegal access has occurred, could not manage case documents");
                    }});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private List<UploadCaseDocumentCollection> findIfDocumentExistInCollectionAfterMove(FinremCaseData caseData,
                                                                                        List<UploadCaseDocumentCollection> collection,
                                                                                        UploadCaseDocumentCollection documentToCheck,
                                                                                        List<UploadCaseDocumentCollection> caseDocumentCollection) {
        for (Iterator<UploadCaseDocumentCollection> it = caseDocumentCollection.iterator(); it.hasNext(); ) {
            UploadCaseDocumentCollection document = it.next();

            if (document.getId().equals(documentToCheck.getId())
                && !document.getValue().getCaseDocuments().equals(documentToCheck.getValue().getCaseDocuments())) {
                collection.remove(documentToCheck);
            } else if (document.getId().equals(documentToCheck.getId())
                && document.getValue().getCaseDocuments().equals(documentToCheck.getValue().getCaseDocuments())) {
                it.remove();
            }
        }

        caseData.getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocumentCollection);
        return collection;
    }

    private Map<String, Field> findCollectionNameOfDocument(FinremCaseData caseData) throws IllegalAccessException {

        Map<String, Field> documentIdToFieldMap = new HashMap<>();
        List<UUID> manageCaseDocumentsCollectionIds = getManageCaseDocumentsCollectionIds(caseData);

        for (Field collectionType : contestedUploadCaseFilesCollectionFields) {
            collectionType.setAccessible(true);
            List<UploadCaseDocumentCollection> docsInCollection =
                getAllDocumentsInCollection(caseData.getUploadCaseDocumentWrapper(), collectionType);

            if (!docsInCollection.isEmpty()) {
                for (UploadCaseDocumentCollection selectedCollectionDocument : docsInCollection) {
                    for (UUID documentId : manageCaseDocumentsCollectionIds) {
                        if (documentId.equals(selectedCollectionDocument.getId())) {
                            documentIdToFieldMap.put(documentId.toString(), collectionType);
                        }
                    }
                }
            }
        }

        return documentIdToFieldMap;
    }

    private List<UUID> getManageCaseDocumentsCollectionIds(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection()).orElse(new ArrayList<>())
            .stream()
            .map(UploadCaseDocumentCollection::getId).collect(Collectors.toList());
    }

    private List<UploadCaseDocumentCollection> getAllDocumentsInCollection(UploadCaseDocumentWrapper data,
                                                                           Field collection) throws IllegalAccessException {
        collection.setAccessible(true);
        List<UploadCaseDocumentCollection> uploadedDocuments = (List<UploadCaseDocumentCollection>) collection.get(data);

        return Optional.ofNullable(uploadedDocuments).orElse(new ArrayList<>());
    }

    private List<UploadCaseDocumentCollection> getManageDocumentCollection(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection()).orElse(new ArrayList<>());
    }
}