package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsService {

    private static final String APPLICANT = "applicant";

    private final ObjectMapper mapper;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) {

        List<ContestedUploadedDocumentData> uploadedDocuments = getUploadedDocuments(caseData);
        List<ApplicantUploadedDocumentData> applicantUploadedDocuments = getApplicantDocuments(caseData);

        for (ContestedUploadedDocumentData item : uploadedDocuments) {
            if (item.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT)) {
                //we need to build ApplicantUploadedDocumentData, and add the "Applicant" marked doc to the collection
                ContestedUploadedDocument document = item.getUploadedCaseDocument();
                ApplicantUploadedDocumentData applicantCaseDocuments = ApplicantUploadedDocumentData.builder()
                    .applicantCaseDocument(document)
                    .build();

                applicantUploadedDocuments.add(applicantCaseDocuments);
            }
        }

        log.info("Generated ApplicantCaseDocuments Collection = {}", applicantUploadedDocuments);

        caseData.put(APPLICANT_CASE_DOCUMENTS, applicantUploadedDocuments);
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);

        return caseData;
    }

    private List<ContestedUploadedDocumentData> getUploadedDocuments(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(CONTESTED_UPLOADED_DOCUMENTS))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(CONTESTED_UPLOADED_DOCUMENTS),
            new TypeReference<List<ContestedUploadedDocumentData>>() {
            });
    }

    private List<ApplicantUploadedDocumentData> getApplicantDocuments(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_CASE_DOCUMENTS))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_CASE_DOCUMENTS),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    //  public void cleanupUploadCollection(Map<String, Object> caseData) {
    //      List<ContestedUploadedDocumentData> uploadedDocuments = getUploadedDocuments(caseData);
    //
    //      for (ContestedUploadedDocumentData item : uploadedDocuments) {
    //           if (item.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT)) {
    //             uploadedDocuments.remove(item);
    //           }
    //      }
    //  }
}
