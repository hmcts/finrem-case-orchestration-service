package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedUploadDocumentsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseDocumentsService {

    private final ObjectMapper mapper;
    private final ContestedUploadDocumentsHelper contestedUploadDocumentsHelper;

    public Map<String, Object> setApplicantAndRespondentDocumentsCollection(CaseDetails caseDetails) {

        caseDetails.getData().put(CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION,
            getAllContestedUploadDocuments(caseDetails.getData()));

        return caseDetails.getData();
    }

    public Map<String, Object> manageLitigantDocuments(Map<String, Object> caseData) {

        removeDeletedFilesFromCaseData(caseData);

        Map<String, String> idToCollectionData = findCollectionNameOfDocument(caseData);

        findAndRemoveMovedDocumentFromCollections(caseData, idToCollectionData);

        contestedUploadDocumentsHelper.setUploadedDocumentsToCollections(caseData, CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION);

        return caseData;
    }

    private void removeDeletedFilesFromCaseData(Map<String, Object> caseData) {

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            mapper.convertValue(caseData.get(CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION),
                    new TypeReference<List<ContestedUploadedDocumentData>>() {
                    }).stream().map(ContestedUploadedDocumentData::getId)
                .collect(Collectors.toSet());

        Arrays.stream(ContestedUploadCaseFilesCollectionType.values()).forEach(collection ->
            Optional.ofNullable(caseData.get(collection.getCcdKey()))
                .ifPresent(mapValue -> caseData.put(collection.getCcdKey(), mapper.convertValue(mapValue,
                        new TypeReference<List<ContestedUploadedDocumentData>>() {
                        })
                    .stream().filter(contestedUploadedDocumentData -> findRemainingApplicantAndRespondentDocumentIds.contains(
                        contestedUploadedDocumentData.getId())).collect(Collectors.toList()))));
    }

    private List<ContestedUploadedDocumentData> getAllContestedUploadDocuments(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> allDocuments = new ArrayList<>();

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {
            Optional.ofNullable(caseData.get(collectionType.getCcdKey()))
                .ifPresent(mapValue -> allDocuments.addAll(mapper.convertValue(mapValue, new TypeReference<>() {})));
        }

        return allDocuments;
    }

    private void findAndRemoveMovedDocumentFromCollections(Map<String, Object> caseData, Map<String, String> documentIdsAndCollection) {

        documentIdsAndCollection.forEach((documentId, collection) -> getAllDocumentsInCollection(caseData, collection)
            .forEach(contestedUploadedDocumentData -> {
                if (collection.startsWith("app")) {
                    caseData.put(collection, findIfDocumentExistInCollectionAfterMove(caseData, contestedUploadedDocumentData,
                         getAllDocumentsInCollection(caseData, collection), "applicant"));
                } else if (collection.startsWith("resp")) {
                    caseData.put(collection, findIfDocumentExistInCollectionAfterMove(caseData, contestedUploadedDocumentData,
                         getAllDocumentsInCollection(caseData, collection), "respondent"));
                }
            }));
    }

    private List<ContestedUploadedDocumentData> findIfDocumentExistInCollectionAfterMove(Map<String, Object> caseData, ContestedUploadedDocumentData documentToCheck,
                                                                                         List<ContestedUploadedDocumentData> collectionToCheck,
                                                                                         String party) {

        for (ContestedUploadedDocumentData document :
            getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION)) {

            if (documentToCheck.getId().equals(document.getId())
                && !document.getUploadedCaseDocument().getCaseDocumentParty().equals(party)) {

                collectionToCheck.remove(documentToCheck);
            }
        }

        return collectionToCheck;
    }

    private Map<String, String> findCollectionNameOfDocument(Map<String, Object> caseData) {

        Map<String, String> documentIdToCollection = new HashMap<>();
        List<String> litigantDocumentsCollection = getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION)
            .stream().map(ContestedUploadedDocumentData::getId).collect(Collectors.toList());

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {

            String collection = collectionType.getCcdKey();

            List<ContestedUploadedDocumentData> docsInCollection = getAllDocumentsInCollection(caseData, collection);

            if (!docsInCollection.isEmpty()) {
                for (ContestedUploadedDocumentData document : docsInCollection) {
                    for (String litigantDocument : litigantDocumentsCollection) {
                        if (litigantDocument.equals(document.getId())) {
                            documentIdToCollection.put(litigantDocument, collection);
                        }
                    }
                }
            }
        }

        return documentIdToCollection;
    }

    private List<ContestedUploadedDocumentData> getAllDocumentsInCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {});
    }
}


