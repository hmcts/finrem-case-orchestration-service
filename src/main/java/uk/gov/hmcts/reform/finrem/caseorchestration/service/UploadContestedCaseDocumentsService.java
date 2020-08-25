package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApplicantUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsService {

    private static final String APPLICANT = "applicant";

    private final ObjectMapper mapper;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) throws Exception {

        List<ContestedUploadedDocumentData> uploadedDocuments = getUploadedDocuments(caseData);
        List<ApplicantUploadedDocumentData> applicantCorrespondenceCollection = getApplicantCorrespondenceCollection(caseData);
        List<ApplicantUploadedDocumentData> applicantFormCollection = getApplicantFormCollection(caseData);
        List<ApplicantUploadedDocumentData> applicantEvidenceCollection = getApplicantEvidenceCollection(caseData);
        List<ApplicantUploadedDocumentData> applicantTrialBundleCollection = getApplicantTrialBundleCollection(caseData);
        String documentType;

        Iterator<ContestedUploadedDocumentData> iterator = uploadedDocuments.iterator();

        for (ContestedUploadedDocumentData item : uploadedDocuments) {
            if (item.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT)) {

                //we need to build ApplicantUploadedDocumentData, and add the "Applicant" marked doc a collection
                ContestedUploadedDocument document = item.getUploadedCaseDocument();
                ApplicantUploadedDocumentData applicantCaseDocuments = ApplicantUploadedDocumentData.builder()
                    .applicantCaseDocument(document)
                    .build();

                documentType = findDocumentType(item);
                switch (documentType) {
                    case "Correspondence":
                        applicantCorrespondenceCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant Correspondence Collection", applicantCaseDocuments);
                        break;
                    case "FR Forms":
                        applicantFormCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant FR Forms Collection", applicantCaseDocuments);
                        break;
                    case "Evidence In Support":
                        applicantEvidenceCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant Evidence In Support Collection", applicantCaseDocuments);
                        break;
                    case "Trial Bundle":
                        applicantTrialBundleCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant Trial Bundle Collection", applicantCaseDocuments);
                        break;
                    default:
                        throw new Exception();
                }
            }
        }

        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
        caseData.put(APPLICANT_CORRESPONDENCE_COLLECTION, applicantCorrespondenceCollection);
        caseData.put(APPLICANT_FR_FORM_COLLECTION, applicantFormCollection);
        caseData.put(APPLICANT_EVIDENCE_COLLECTION, applicantEvidenceCollection);
        caseData.put(APPLICANT_TRIAL_BUNDLE_COLLECTION, applicantTrialBundleCollection);

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

    private List<ApplicantUploadedDocumentData> getApplicantCorrespondenceCollection(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_CORRESPONDENCE_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_CORRESPONDENCE_COLLECTION),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    private List<ApplicantUploadedDocumentData> getApplicantFormCollection(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_FR_FORM_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_FR_FORM_COLLECTION),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    private List<ApplicantUploadedDocumentData> getApplicantEvidenceCollection(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_EVIDENCE_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_EVIDENCE_COLLECTION),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    private List<ApplicantUploadedDocumentData> getApplicantTrialBundleCollection(Map<String, Object> caseData) {

        if (StringUtils.isEmpty(caseData.get(APPLICANT_TRIAL_BUNDLE_COLLECTION))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(APPLICANT_TRIAL_BUNDLE_COLLECTION),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    private String findDocumentType(ContestedUploadedDocumentData item) {
        String documentType = item.getUploadedCaseDocument().getCaseDocumentType();
        return documentTypesMap.getOrDefault(documentType, "");
    }

    private Map<String, String> documentTypesMap = ImmutableMap.<String, String>builder()
        .put("Letter from Applicant", "Correspondence")
        .put("Form B", "FR Forms")
        .put("Form E", "FR Forms")
        .put("Form F", "FR Forms")
        .put("Form G", "FR Forms")
        .put("Form H (Costs estimate)", "FR Forms")
        .put("Statement of Issues", "Evidence In Support")
        .put("Chronology", "Evidence In Support")
        .put("Case Summary", "Evidence In Support")
        .put("Questionnaire", "Evidence In Support")
        .put("Reply to Questionnaire", "Evidence In Support")
        .put("Valuation Report", "Evidence In Support")
        .put("Pension Plan", "Evidence In Support")
        .put("Position Statement", "Evidence In Support")
        .put("Skeleton Argument", "Evidence In Support")
        .put("Expert Evidence", "Evidence In Support")
        .put("Witness Statement/Affidavit", "Evidence In Support")
        .put("Care Plan", "Evidence In Support")
        .put("Offers", "Evidence In Support")
        .put("Other", "Evidence In Support")
        .put("Trial Bundle", "Trial Bundle")
        .build();


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
