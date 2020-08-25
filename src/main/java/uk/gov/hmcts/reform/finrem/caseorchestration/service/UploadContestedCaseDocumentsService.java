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
    private static final String CORRESPONDENCE = "Correspondence";
    private static final String FR_FORMS = "FR Forms";
    private static final String EVIDENCE_IN_SUPPORT = "Evidence In Support";
    private static final String TRIAL_BUNDLE = "Trial Bundle";

    private final ObjectMapper mapper;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) throws Exception {

        List<ContestedUploadedDocumentData> uploadedDocuments = getUploadedDocuments(caseData);
        List<ApplicantUploadedDocumentData> applicantCorrespondenceCollection = getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION);
        List<ApplicantUploadedDocumentData> applicantFormCollection = getDocumentCollection(caseData, APPLICANT_FR_FORM_COLLECTION);
        List<ApplicantUploadedDocumentData> applicantEvidenceCollection = getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION);
        List<ApplicantUploadedDocumentData> applicantTrialBundleCollection = getDocumentCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION);
        String documentType;

        for (ContestedUploadedDocumentData item : uploadedDocuments) {
            if (item.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT)) {

                //we need to build ApplicantUploadedDocumentData, and add the "Applicant" marked doc a collection
                ContestedUploadedDocument document = item.getUploadedCaseDocument();
                ApplicantUploadedDocumentData applicantCaseDocuments = ApplicantUploadedDocumentData.builder()
                    .applicantCaseDocument(document)
                    .build();

                documentType = findDocumentType(item);
                switch (documentType) {
                    case CORRESPONDENCE:
                        applicantCorrespondenceCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant Correspondence Collection", applicantCaseDocuments);
                        break;
                    case FR_FORMS:
                        applicantFormCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant FR Forms Collection", applicantCaseDocuments);
                        break;
                    case EVIDENCE_IN_SUPPORT:
                        applicantEvidenceCollection.add(applicantCaseDocuments);
                        log.info("Adding item: {}, to Applicant Evidence In Support Collection", applicantCaseDocuments);
                        break;
                    case TRIAL_BUNDLE:
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

    private List<ApplicantUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection),
            new TypeReference<List<ApplicantUploadedDocumentData>>() {
            });
    }

    private String findDocumentType(ContestedUploadedDocumentData item) {
        String documentType = item.getUploadedCaseDocument().getCaseDocumentType();
        return documentTypesMap.getOrDefault(documentType, "");
    }

    private Map<String, String> documentTypesMap = ImmutableMap.<String, String>builder()
        .put("Letter from Applicant", CORRESPONDENCE)
        .put("Form B", FR_FORMS)
        .put("Form E", FR_FORMS)
        .put("Form F", FR_FORMS)
        .put("Form G", FR_FORMS)
        .put("Form H (Costs estimate)", FR_FORMS)
        .put("Statement of Issues", EVIDENCE_IN_SUPPORT)
        .put("Chronology", EVIDENCE_IN_SUPPORT)
        .put("Case Summary", EVIDENCE_IN_SUPPORT)
        .put("Questionnaire", EVIDENCE_IN_SUPPORT)
        .put("Reply to Questionnaire", EVIDENCE_IN_SUPPORT)
        .put("Valuation Report", EVIDENCE_IN_SUPPORT)
        .put("Pension Plan", EVIDENCE_IN_SUPPORT)
        .put("Position Statement", EVIDENCE_IN_SUPPORT)
        .put("Skeleton Argument", EVIDENCE_IN_SUPPORT)
        .put("Expert Evidence", EVIDENCE_IN_SUPPORT)
        .put("Witness Statement/Affidavit", EVIDENCE_IN_SUPPORT)
        .put("Care Plan", EVIDENCE_IN_SUPPORT)
        .put("Offers", EVIDENCE_IN_SUPPORT)
        .put("Other", EVIDENCE_IN_SUPPORT)
        .put("Trial Bundle", TRIAL_BUNDLE)
        .build();
}
