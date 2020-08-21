package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsService {

    private final ObjectMapper mapper;

    public void filterDocumentsToRelevantParty(Map<String, Object> caseData) {

        List<ContestedUploadedDocument> uploadedDocuments = getUploadedDocuments(caseData);
        List<ContestedUploadedDocument> applicantUploadedDocuments = getApplicantDocuments(caseData);

        for (ContestedUploadedDocument item : uploadedDocuments) {
            if (item.getCaseDocumentParty().equals("Applicant")) {
                uploadedDocuments.remove(item);
                applicantUploadedDocuments.add(item);
            }
        }

        ApplicantUploadedDocumentData applicantCaseDocuments = ApplicantUploadedDocumentData.builder()
            .applicantCaseDocuments(applicantUploadedDocuments)
            .build();

        log.info("Generated ApplicantCaseDocuments Collection = {}", applicantCaseDocuments);

        List<ApplicantUploadedDocumentData> applicantDocuments = asList(applicantCaseDocuments);
        caseData.put(APPLICANT_CASE_DOCUMENTS, applicantDocuments);
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
    }

    private List<ContestedUploadedDocument> getUploadedDocuments(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(CONTESTED_UPLOADED_DOCUMENTS))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(CONTESTED_UPLOADED_DOCUMENTS),
            new TypeReference<List<ContestedUploadedDocument>>() {
            });
    }

    private List<ContestedUploadedDocument> getApplicantDocuments(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_CASE_DOCUMENTS))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_CASE_DOCUMENTS),
            new TypeReference<List<ContestedUploadedDocument>>() {
            });
    }
}
