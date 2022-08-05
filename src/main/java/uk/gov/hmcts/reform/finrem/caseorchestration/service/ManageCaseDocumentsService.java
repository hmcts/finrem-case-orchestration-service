package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.Lists;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseDocumentsService {

    public static final String ILLEGAL_ACCESS_MESSAGE = "Illegal access has occurred, could not manage case documents, reason: ";
    private final List<CaseDocumentHandler<UploadCaseDocumentCollection>> caseDocumentHandlers;
    private final List<Field> contestedUploadCaseFilesCollectionFields = Arrays.stream(ContestedUploadCaseFilesCollectionType.values())
        .map(ContestedUploadCaseFilesCollectionType::getManageCaseDocumentCollectionField).toList();

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
            throw new IllegalStateException(ILLEGAL_ACCESS_MESSAGE);
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
            throw new IllegalStateException(ILLEGAL_ACCESS_MESSAGE);
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
            throw new IllegalStateException(ILLEGAL_ACCESS_MESSAGE + e.getMessage());
        }
    }

    private void replaceCollectionWithValue(FinremCaseData caseData, Set<String> findRemainingApplicantAndRespondentDocumentIds,
                                            Field collection, List<UploadCaseDocumentCollection> value) {
        try {
            collection.set(caseData.getUploadCaseDocumentWrapper(),
                filterRemainingDocuments(findRemainingApplicantAndRespondentDocumentIds, value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(ILLEGAL_ACCESS_MESSAGE + e.getMessage());
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
                removeDocumentFromCollection(caseData, caseDocumentsCollection, uploadCaseDocumentData, collection);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(ILLEGAL_ACCESS_MESSAGE + e.getMessage());
            }
        });
    }

    private void removeDocumentFromCollection(FinremCaseData caseData,
                                              List<UploadCaseDocumentCollection> caseDocumentsCollection,
                                              UploadCaseDocumentWrapper uploadCaseDocumentData,
                                              Field collection) throws IllegalAccessException {
        List<UploadCaseDocumentCollection> allDocumentsInCollection =
            getAllDocumentsInCollection(uploadCaseDocumentData, collection);

        for (ListIterator<UploadCaseDocumentCollection> docCollectionIterator = allDocumentsInCollection.listIterator();
             docCollectionIterator.hasNext();) {
            UploadCaseDocumentCollection document = docCollectionIterator.next();

            collection.set(uploadCaseDocumentData, findIfDocumentExistInCollectionAfterMove(
                caseData, docCollectionIterator, document, caseDocumentsCollection));
        }
    }

    private List<UploadCaseDocumentCollection> findIfDocumentExistInCollectionAfterMove(FinremCaseData caseData,
                                                                                        ListIterator<UploadCaseDocumentCollection> collection,
                                                                                        UploadCaseDocumentCollection documentToCheck,
                                                                                        List<UploadCaseDocumentCollection> caseDocumentCollection) {
        for (Iterator<UploadCaseDocumentCollection> it = caseDocumentCollection.iterator(); it.hasNext(); ) {
            UploadCaseDocumentCollection documentData = it.next();

            if (documentData.getId().equals(documentToCheck.getId()) && !documentData.getValue().equals(documentToCheck.getValue())) {
                collection.remove();
            } else if (documentData.getId().equals(documentToCheck.getId()) && documentData.getValue().equals(documentToCheck.getValue())) {
                it.remove();
            }
        }

        caseData.getUploadCaseDocumentWrapper().setManageCaseDocumentCollection(caseDocumentCollection);
        return getListFromIterator(collection);
    }

    private Map<String, Field> findCollectionNameOfDocument(FinremCaseData caseData) throws IllegalAccessException {

        Map<String, Field> documentIdToFieldMap = new HashMap<>();
        List<UUID> manageCaseDocumentsCollectionIds = getManageCaseDocumentsCollectionIds(caseData);

        for (Field collectionType : contestedUploadCaseFilesCollectionFields) {
            collectionType.setAccessible(true);
            List<UploadCaseDocumentCollection> docsInCollection =
                getAllDocumentsInCollection(caseData.getUploadCaseDocumentWrapper(), collectionType);

            if (!docsInCollection.isEmpty()) {
                docsInCollection.stream()
                    .filter(document -> manageCaseDocumentsCollectionIds.stream().anyMatch(id -> document.getId().equals(id)))
                    .toList()
                    .forEach(matchingDocument -> documentIdToFieldMap.put(matchingDocument.getId().toString(), collectionType));
            }
        }

        return documentIdToFieldMap;
    }

    private List<UUID> getManageCaseDocumentsCollectionIds(FinremCaseData caseData) {
        return Optional.ofNullable(caseData.getUploadCaseDocumentWrapper().getManageCaseDocumentCollection()).orElse(new ArrayList<>())
            .stream()
            .map(UploadCaseDocumentCollection::getId)
            .toList();
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

    private List<UploadCaseDocumentCollection> getListFromIterator(ListIterator<UploadCaseDocumentCollection> collection) {
        while (collection.hasPrevious()) {
            collection.previous();
        }

        return Lists.newArrayList(collection);
    }
}