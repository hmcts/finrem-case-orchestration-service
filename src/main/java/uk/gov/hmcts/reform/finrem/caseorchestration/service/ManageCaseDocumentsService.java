package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
            getAllContestedUploadDocuments(caseDetails.getData()));

        return caseDetails.getData();
    }

    public Map<String, Object> manageLitigantDocuments(Map<String, Object> caseData) {

        removeDeletedFilesFromCaseData(caseData);

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
}


