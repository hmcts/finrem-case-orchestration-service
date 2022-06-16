package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollectionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICANT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseDocumentsService {

    private final ObjectMapper mapper;

    public Map<String, Object> setApplicantAndRespondentDocumentsCollection(CaseDetails caseDetails) {

        caseDetails.getData().put(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED,
            extractFieldsToDocumentDetailsCollection(caseDetails.getData(), APPLICANT));

        caseDetails.getData().put(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED,
            extractFieldsToDocumentDetailsCollection(caseDetails.getData(), RESPONDENT));

        return caseDetails.getData();
    }

    public Map<String, Object> removeDeletedFilesFromCaseData(Map<String, Object> caseData) {

        removeDeletedFilesFromCollections(caseData, ContestedUploadCaseFilesCollectionType.values(),
            CONTESTED_APPLICANT_DOCUMENTS_UPLOADED, CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED);

        caseData.remove(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED);
        caseData.remove(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED);

        return caseData;
    }

    private void removeDeletedFilesFromCollections(Map<String, Object> caseData,
                                                   ContestedUploadCaseFilesCollectionType[] collectionTypes, String... documentType) {

        List<DocumentDetailsData> mergeApplicantAndRespondentDocumentDetailsData = new ArrayList<>();

        Arrays.stream(documentType).forEach(document -> mergeApplicantAndRespondentDocumentDetailsData.addAll(
            mapper.convertValue(caseData.get(document),
                new TypeReference<List<DocumentDetailsData>>() {
                })));

        Set<String> findRemainingApplicantAndRespondentDocumentIds =
            mergeApplicantAndRespondentDocumentDetailsData.stream().map(DocumentDetailsData::getId)
                .collect(Collectors.toSet());

        Arrays.stream(collectionTypes).forEach(collection ->
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

    private List<ContestedUploadedDocumentData> filterApplicantOrRespondentDocuments(Map<String, Object> caseData, String party) {
        return getAllContestedUploadedDocumentsData(caseData).stream()
            .filter(document -> document.getUploadedCaseDocument() != null
                && document.getUploadedCaseDocument().getCaseDocuments() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty().equalsIgnoreCase(party)).collect(Collectors.toList());
    }

    private List<DocumentDetailsData> extractFieldsToDocumentDetailsCollection(Map<String, Object> caseData, String party) {

        return filterApplicantOrRespondentDocuments(caseData, party).stream().filter(
            document -> document.getUploadedCaseDocument().getCaseDocuments() != null
        ).map(detail -> DocumentDetailsData.builder()
            .id(detail.getId())
            .documentDetails(DocumentDetailsCollection.builder()
                .documentFileName(detail.getUploadedCaseDocument().getCaseDocuments().getDocumentFilename())
                .documentType(detail.getUploadedCaseDocument().getCaseDocumentType())
                .build()).build()).collect(Collectors.toList());
    }
}
