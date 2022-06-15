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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentDetailsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ContestedUploadCaseFilesCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        removeDeletedFilesFromCollections(caseData, ContestedUploadCaseFilesCollections.values(),
            CONTESTED_APPLICANT_DOCUMENTS_UPLOADED, CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED);

        caseData.remove(CONTESTED_APPLICANT_DOCUMENTS_UPLOADED);
        caseData.remove(CONTESTED_RESPONDENT_DOCUMENTS_UPLOADED);

        return caseData;
    }

    private void removeDeletedFilesFromCollections(Map<String, Object> caseData,
                                                   ContestedUploadCaseFilesCollections[] collectionTypes, String... documentType) {

        List<DocumentDetailsData> allDocumentDetailsData = new ArrayList<>();

        for (String document : documentType) {

            allDocumentDetailsData.addAll(mapper.convertValue(caseData.get(document), new TypeReference<List<DocumentDetailsData>>() {
            }));
        }

        Set<String> remainingDocumentsInCollection = allDocumentDetailsData.stream().map(DocumentDetailsData::getId).collect(Collectors.toSet());

        for (ContestedUploadCaseFilesCollections collection : collectionTypes) {

            if (caseData.get(collection.toString()) == null) {
                continue;
            }

            caseData.put(collection.toString(), mapper.convertValue(caseData.get(collection.toString()),
                    new TypeReference<List<ContestedUploadedDocumentData>>() {
                    })
                .stream().filter(contestedUploadedDocumentData -> remainingDocumentsInCollection.contains(
                    contestedUploadedDocumentData.getId())).collect(Collectors.toList()));
        }
    }


    private List<ContestedUploadedDocumentData> getAllUploadedDocuments(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> allDocuments = new ArrayList<>();

        for (ContestedUploadCaseFilesCollections collection : ContestedUploadCaseFilesCollections.values()) {

            if (caseData.get(collection.toString()) != null) {
                allDocuments.addAll(getDocumentCollection(caseData, collection.toString()));
            }
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
        return getAllUploadedDocuments(caseData).stream()
            .filter(document -> document.getUploadedCaseDocument() != null
                && document.getUploadedCaseDocument().getCaseDocuments() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty() != null
                && document.getUploadedCaseDocument().getCaseDocumentParty().equals(party)).collect(Collectors.toList());
    }

    private List<DocumentDetailsData> extractFieldsToDocumentDetailsCollection(Map<String, Object> caseData, String party) {

        List<DocumentDetailsData> documents = new ArrayList<>();

        for (ContestedUploadedDocumentData documentDetail : filterApplicantOrRespondentDocuments(caseData, party)) {
            DocumentDetailsData documentDetailsData = new DocumentDetailsData();
            DocumentDetails details = new DocumentDetails();

            ContestedUploadedDocument uploadedDocument = documentDetail.getUploadedCaseDocument();

            if (uploadedDocument.getCaseDocuments() != null) {
                details.setDocumentFileName(uploadedDocument.getCaseDocuments().getDocumentFilename());
            }

            documentDetailsData.setId(documentDetail.getId());
            details.setDocumentType(uploadedDocument.getCaseDocumentType());

            documentDetailsData.setDocumentDetails(details);

            documents.add(documentDetailsData);
        }

        return documents;
    }
}
