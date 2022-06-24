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
            filterApplicantOrRespondentDocuments(caseDetails.getData()));

        return caseDetails.getData();
    }

    public Map<String, Object> manageLitigantDocuments(Map<String, Object> caseData) {

        contestedUploadDocumentsHelper.setUploadedDocumentsToCollections(caseData, CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION);
        findAndRemoveMovedDocumentInCollections(caseData);
        removeDeletedFilesFromCaseData(caseData);

        return caseData;
    }

    private void removeDeletedFilesFromCaseData(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> mergeApplicantAndRespondentDocumentDetailsData =
            new ArrayList<>(mapper.convertValue(caseData.get(CONTESTED_MANAGE_LITIGANT_DOCUMENTS_COLLECTION),
            new TypeReference<List<ContestedUploadedDocumentData>>() {}));

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            mergeApplicantAndRespondentDocumentDetailsData.stream().map(ContestedUploadedDocumentData::getId)
                .collect(Collectors.toSet());

        Arrays.stream(ContestedUploadCaseFilesCollectionType.values()).forEach(collection ->
            Optional.ofNullable(caseData.get(collection.getCcdKey()))
                .ifPresent(mapValue -> caseData.put(collection.getCcdKey(), mapper.convertValue(mapValue,
                        new TypeReference<List<ContestedUploadedDocumentData>>() {
                        })
                    .stream().filter(contestedUploadedDocumentData -> findRemainingApplicantAndRespondentDocumentIds.contains(
                        contestedUploadedDocumentData.getId())).collect(Collectors.toList()))));
    }

    private List<ContestedUploadedDocumentData> getAllContestedUploadedDocumentsData(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> allDocuments = new ArrayList<>();

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {
            Optional.ofNullable(caseData.get(collectionType.getCcdKey()))
                .ifPresent(mapValue -> allDocuments.addAll(mapper.convertValue(mapValue, new TypeReference<>() {})));
        }

        return allDocuments;
    }

    private List<ContestedUploadedDocumentData> filterApplicantOrRespondentDocuments(Map<String, Object> caseData) {
        return getAllContestedUploadedDocumentsData(caseData).stream()
            .filter(document -> document.getUploadedCaseDocument() != null
                && document.getUploadedCaseDocument().getCaseDocuments() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty() != null).collect(Collectors.toList());
    }

    private void findAndRemoveMovedDocumentInCollections(Map<String, Object> caseData) {

        for (ContestedUploadCaseFilesCollectionType collection : ContestedUploadCaseFilesCollectionType.values()) {

            List<ContestedUploadedDocumentData> docsInCollection = new ArrayList<>();

            Optional.ofNullable(caseData.get(collection.getCcdKey()))
                .ifPresent(mapValue -> docsInCollection.addAll(mapper.convertValue(mapValue, new TypeReference<>() {})));

            if (!docsInCollection.isEmpty()) {
                if (collection.getCcdKey().startsWith("app")) {
                    filterMovedDocumentsInCollection(caseData, "applicant", collection.getCcdKey(), docsInCollection);
                } else if (collection.getCcdKey().startsWith("resp")) {
                    filterMovedDocumentsInCollection(caseData, "respondent", collection.getCcdKey(), docsInCollection);
                }
            }
        }
    }

    private void filterMovedDocumentsInCollection(Map<String, Object> caseData, String party,
                                                  String collection, List<ContestedUploadedDocumentData> docsInCollection) {

        List<ContestedUploadedDocumentData> collectionFiltered = docsInCollection.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party)).collect(Collectors.toList());

        List<ContestedUploadedDocumentData> getFromCaseData = getDocumentCollection(caseData, collection);
        getFromCaseData.addAll(collectionFiltered);

        if (!getFromCaseData.isEmpty()) {
            caseData.put(collection, getFromCaseData);
        }
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {});
    }
}
