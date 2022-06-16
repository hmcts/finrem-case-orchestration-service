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

        List<DocumentDetailsData> allDocumentDetailsData = new ArrayList<>();

        Arrays.stream(documentType).forEach(document -> allDocumentDetailsData.addAll(
            mapper.convertValue(caseData.get(document),
                new TypeReference<List<DocumentDetailsData>>() {
                })));

        Set<String> remainingDocumentsInCollection = allDocumentDetailsData.stream().map(DocumentDetailsData::getId).collect(Collectors.toSet());

        for (ContestedUploadCaseFilesCollectionType collection : collectionTypes) {

            caseData.computeIfPresent(collection.toString(), (key, value) -> mapper.convertValue(caseData.get(key),
                    new TypeReference<List<ContestedUploadedDocumentData>>() {
                    })
                .stream().filter(contestedUploadedDocumentData -> remainingDocumentsInCollection.contains(
                    contestedUploadedDocumentData.getId())).collect(Collectors.toList()));
        }
    }


    private List<ContestedUploadedDocumentData> getAllContestedUploadedDocumentsData(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> allDocuments = new ArrayList<>();

        for (ContestedUploadCaseFilesCollectionType collectionType : ContestedUploadCaseFilesCollectionType.values()) {
            Optional.ofNullable(caseData.get(collectionType.getCcdKey()))
                .ifPresent(mapValue -> allDocuments.addAll(mapper.convertValue(mapValue, new TypeReference<>() {})));
        }

        return allDocuments;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection), new TypeReference<>() {});
    }

    private List<ContestedUploadedDocumentData> filterApplicantOrRespondentDocuments(Map<String, Object> caseData, String party) {
        return getAllContestedUploadedDocumentsData(caseData).stream()
            .filter(document -> document.getUploadedCaseDocument() != null
                && document.getUploadedCaseDocument().getCaseDocuments() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty().equals(party)).collect(Collectors.toList());
    }

    private List<DocumentDetailsData> extractFieldsToDocumentDetailsCollection(Map<String, Object> caseData, String party) {

        List<DocumentDetailsData> documents = new ArrayList<>();

        for (ContestedUploadedDocumentData documentDetail : filterApplicantOrRespondentDocuments(caseData, party)) {

            ContestedUploadedDocument uploadedDocument = documentDetail.getUploadedCaseDocument();

            if (uploadedDocument.getCaseDocuments() != null) {
                documents.add(DocumentDetailsData.builder()
                    .id(documentDetail.getId())
                    .documentDetails(DocumentDetailsCollection.builder()
                        .documentFileName(uploadedDocument.getCaseDocuments().getDocumentFilename())
                        .documentType(uploadedDocument.getCaseDocumentType()).build()).build());
            } else {
                documents.add(DocumentDetailsData.builder()
                    .id(documentDetail.getId())
                    .documentDetails(DocumentDetailsCollection.builder()
                        .documentType(uploadedDocument.getCaseDocumentType()).build()).build());
            }
        }

        return documents;
    }
}
