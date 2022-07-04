package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private final List<CaseDocumentHandler> caseDocumentHandlers;

    public Map<String, Object> setApplicantAndRespondentDocumentsCollection(CaseDetails caseDetails) {

        caseDetails.getData().put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION,
            getAllContestedUploadDocuments(caseDetails.getData()));

        return caseDetails.getData();
    }

    public Map<String, Object> manageCaseDocuments(Map<String, Object> caseData) {

        removeDeletedFilesFromCaseData(caseData);

        Map<String, String> idToCollectionData = findCollectionNameOfDocument(caseData);

        findAndRemoveMovedDocumentFromCollections(caseData, idToCollectionData);

        List<ContestedUploadedDocumentData> caseDocuments = getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION);
        caseDocumentHandlers.forEach(h -> h.handle(caseDocuments, caseData));
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, caseDocuments);

        return caseData;
    }

    private void removeDeletedFilesFromCaseData(Map<String, Object> caseData) {

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            mapper.convertValue(caseData.get(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION),
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
                .ifPresent(mapValue -> allDocuments.addAll(mapper.convertValue(mapValue, new TypeReference<>() {
                })));
        }

        return allDocuments;
    }

    private void findAndRemoveMovedDocumentFromCollections(Map<String, Object> caseData, Map<String, String> documentIdsAndCollection) {

        List<ContestedUploadedDocumentData> caseDocumentsCollection = getAllDocumentsInCollection(caseData,
            CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION);
        documentIdsAndCollection.forEach((documentId, collection) ->
            getAllDocumentsInCollection(caseData, collection)
            .forEach(contestedUploadedDocumentData -> {
                if (collection.startsWith("app")) {
                    caseData.put(collection, findIfDocumentExistInCollectionAfterMove(
                        caseData, getAllDocumentsInCollection(caseData, collection), contestedUploadedDocumentData,
                        caseDocumentsCollection, "applicant"));
                } else if (collection.startsWith("resp")) {
                    caseData.put(collection, findIfDocumentExistInCollectionAfterMove(
                        caseData, getAllDocumentsInCollection(caseData, collection), contestedUploadedDocumentData,
                        caseDocumentsCollection, "respondent"));
                }
            }));
    }

    private List<ContestedUploadedDocumentData> findIfDocumentExistInCollectionAfterMove(Map<String, Object> caseData,
                                                                      List<ContestedUploadedDocumentData> collection,
                                                                      ContestedUploadedDocumentData documentToCheck,
                                                                      List<ContestedUploadedDocumentData> caseDocumentCollection,
                                                                      String party) {

        for (Iterator<ContestedUploadedDocumentData> it = caseDocumentCollection.iterator(); it.hasNext(); ) {

            ContestedUploadedDocumentData document = it.next();

            ContestedUploadedDocument uploadedDocument = document.getUploadedCaseDocument();
            ContestedUploadedDocument check = documentToCheck.getUploadedCaseDocument();

            if (documentToCheck.getId().equals(document.getId())
                && (!uploadedDocument.getCaseDocuments().equals(check.getCaseDocuments())
                || !uploadedDocument.getCaseDocumentParty().equals(party)
                || !uploadedDocument.getCaseDocumentType()
                .equals(check.getCaseDocumentType())
                || !uploadedDocument.getHearingDetails()
                .equals(check.getHearingDetails()))) {

                collection.remove(documentToCheck);

            } else if (documentToCheck.getId().equals(document.getId())
                && uploadedDocument.getCaseDocumentParty().equals(party)
                && uploadedDocument.getCaseDocumentType().equals(check.getCaseDocumentType())
                && uploadedDocument.getHearingDetails().equals(check.getHearingDetails())
                && uploadedDocument.getCaseDocuments().equals(
                check.getCaseDocuments())) {
                it.remove();
            }
        }

        caseData.put(CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION, caseDocumentCollection);
        return collection;
    }

    private Map<String, String> findCollectionNameOfDocument(Map<String, Object> caseData) {

        Map<String, String> documentIdToCollection = new HashMap<>();
        List<String> caseDocumentsCollection = getAllDocumentsInCollection(caseData, CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION)
            .stream().map(ContestedUploadedDocumentData::getId).collect(Collectors.toList());

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {

            String collection = collectionType.getCcdKey();

            List<ContestedUploadedDocumentData> docsInCollection = getAllDocumentsInCollection(caseData, collection);

            if (!docsInCollection.isEmpty()) {
                for (ContestedUploadedDocumentData document : docsInCollection) {
                    for (String caseDocument : caseDocumentsCollection) {
                        if (caseDocument.equals(document.getId())) {
                            documentIdToCollection.put(caseDocument, collection);
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

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }
}


