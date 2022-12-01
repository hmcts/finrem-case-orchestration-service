package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseDocumentsService {

    private final ObjectMapper mapper;
    private final List<CaseDocumentManager<UploadCaseDocumentCollection>> caseDocumentHandlers;

    public Map<String, Object> manageCaseDocuments(Map<String, Object> caseData) {
        deleteRemovedDocumentsFromCollections(caseData);

        moveDocuments(caseData, idToCollectionData);

        List<UploadCaseDocumentCollection> caseDocuments = getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION);
        caseDocumentHandlers.forEach(h -> h.manageDocumentCollection(caseDocuments, caseData));
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, caseDocuments);

        return caseData;
    }

    private void deleteRemovedDocumentsFromCollections(Map<String, Object> caseData) {

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            mapper.convertValue(caseData.get(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION),
                    new TypeReference<List<UploadCaseDocumentCollection>>() {
                    }).stream().map(UploadCaseDocumentCollection::getId)
                .collect(Collectors.toSet());

        Arrays.stream(ContestedUploadCaseFilesCollectionType.values()).forEach(collection ->
            Optional.ofNullable(caseData.get(collection.getCcdKey()))
                .ifPresent(mapValue -> caseData.put(collection.getCcdKey(), mapper.convertValue(mapValue,
                        new TypeReference<List<UploadCaseDocumentCollection>>() {
                        })
                    .stream().filter(contestedUploadedDocumentData -> findRemainingApplicantAndRespondentDocumentIds.contains(
                        contestedUploadedDocumentData.getId())).collect(Collectors.toList()))));
    }

    private List<UploadCaseDocumentCollection> getAllContestedUploadDocuments(Map<String, Object> caseData) {

        List<UploadCaseDocumentCollection> allDocuments = new ArrayList<>();

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {
            Optional.ofNullable(caseData.get(collectionType.getCcdKey()))
                .ifPresent(mapValue -> allDocuments.addAll(mapper.convertValue(mapValue, new TypeReference<>() {
                })));
        }

        return allDocuments;
    }

    private void moveDocuments(Map<String, Object> caseData, Map<String, String> documentIdsAndCollection) {

        List<UploadCaseDocumentCollection> caseDocumentsCollection = getAllDocumentsInCollection(caseData,
            CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION);
        documentIdsAndCollection.forEach((documentId, collection) ->
            getAllDocumentsInCollection(caseData, collection)
                .forEach(contestedUploadedDocumentData -> caseData.put(collection, findIfDocumentExistInCollectionAfterMove(
                    caseData, getAllDocumentsInCollection(caseData, collection), contestedUploadedDocumentData,
                    caseDocumentsCollection))));
    }

    private List<UploadCaseDocumentCollection> findIfDocumentExistInCollectionAfterMove(Map<String, Object> caseData,
                                                                                         List<UploadCaseDocumentCollection> collection,
                                                                                         UploadCaseDocumentCollection documentToCheck,
                                                                                         List<UploadCaseDocumentCollection> caseDocumentCollection) {

        for (Iterator<UploadCaseDocumentCollection> it = caseDocumentCollection.iterator(); it.hasNext(); ) {

            UploadCaseDocumentCollection document = it.next();

            if (document.getId().equals(documentToCheck.getId())
                && !document.getUploadCaseDocument().equals(documentToCheck.getUploadCaseDocument())) {

                collection.remove(documentToCheck);
            } else if (document.getId().equals(documentToCheck.getId())
                && document.getUploadCaseDocument().equals(documentToCheck.getUploadCaseDocument())) {
                it.remove();
            }
        }

        caseData.put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocumentCollection);
        return collection;
    }

    public Map<String, Object> setCaseDataBeforeManageCaseDocumentCollection(Map<String, Object> caseData, Map<String, Object> caseDataBefore) {
        List<String> manageCaseDocumentCollectionIds = getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION)
            .stream().map(UploadCaseDocumentCollection::getId).collect(Collectors.toList());

        List<UploadCaseDocumentCollection> caseDataBeforeManagedCaseDocumentCollection = new ArrayList<>();
        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {
            List<UploadCaseDocumentCollection> docsInCollection = getAllDocumentsInCollection(caseDataBefore, collectionType.getCcdKey());

            if (!docsInCollection.isEmpty()) {
                List<UploadCaseDocumentCollection> documentData = docsInCollection.stream()
                    .filter(document -> manageCaseDocumentCollectionIds.stream()
                        .anyMatch(caseDocumentId -> document.getId().equals(caseDocumentId)))
                    .collect(Collectors.toList());

                caseDataBeforeManagedCaseDocumentCollection.addAll(documentData);
            }
        }

        if (!caseDataBeforeManagedCaseDocumentCollection.isEmpty()) {
            caseDataBefore.put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDataBeforeManagedCaseDocumentCollection);
        }
        return caseDataBefore;
    }

    private List<UploadCaseDocumentCollection> getAllDocumentsInCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }
}