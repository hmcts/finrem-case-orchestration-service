package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<ContestedUploadedDocumentData> uploadedDocuments = getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS);

        //we need to build ApplicantUploadedDocumentData, and add the "Applicant" marked doc a collection
        List<ContestedUploadedDocumentData> correspondenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType().equals("Letter from Applicant"))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantCorrespondenceCollection = getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION);
        applicantCorrespondenceCollection.addAll(correspondenceFiltered);
        log.info("Adding item: {}, to Applicant Correspondence Collection", correspondenceFiltered);

        List<ContestedUploadedDocumentData> frFormsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType().equals("Form B")
                    || d.getUploadedCaseDocument().getCaseDocumentType().equals("Applicant - Form E")
                    || d.getUploadedCaseDocument().getCaseDocumentType().equals("Form F")
                    || d.getUploadedCaseDocument().getCaseDocumentType().equals("Form G")
                    || d.getUploadedCaseDocument().getCaseDocumentType().equals("Form H"))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantFormCollection = getDocumentCollection(caseData, APPLICANT_FR_FORM_COLLECTION);
        applicantFormCollection.addAll(frFormsFiltered);
        log.info("Adding item: {}, to Applicant FR Forms Collection", frFormsFiltered);

        List<ContestedUploadedDocumentData> evidenceInSupportFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType().equals("Statement of Issues")
                    || d.getUploadedCaseDocument().getCaseDocumentType().equals("Chronology")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Case Summary")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Questionnaire")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Reply to Questionnaire")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Valuation Report")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Pension Plan")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Position Statement")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Skeleton Argument")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Expert Evidence")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Witness Statement/Affidavit")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Care Plan")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("Offers")
                || d.getUploadedCaseDocument().getCaseDocumentType().equals("other"))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantEvidenceCollection = getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION);
        applicantEvidenceCollection.addAll(evidenceInSupportFiltered);
        log.info("Adding item: {}, to Applicant Evidence In Support Collection", evidenceInSupportFiltered);

        List<ContestedUploadedDocumentData> trialBundleFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType().equals("Trial Bundle"))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantTrialBundleCollection = getDocumentCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION);
        applicantTrialBundleCollection.addAll(trialBundleFiltered);
        log.info("Adding item: {}, to Applicant Trial Bundle Collection", trialBundleFiltered);

        uploadedDocuments.removeAll(correspondenceFiltered);
        uploadedDocuments.removeAll(frFormsFiltered);
        uploadedDocuments.removeAll(evidenceInSupportFiltered);
        uploadedDocuments.removeAll(trialBundleFiltered);

        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
        caseData.put(APPLICANT_CORRESPONDENCE_COLLECTION, applicantCorrespondenceCollection);
        caseData.put(APPLICANT_FR_FORM_COLLECTION, applicantFormCollection);
        caseData.put(APPLICANT_EVIDENCE_COLLECTION, applicantEvidenceCollection);
        caseData.put(APPLICANT_TRIAL_BUNDLE_COLLECTION, applicantTrialBundleCollection);

        return caseData;
    }

    private List<ContestedUploadedDocumentData> getDocumentCollection(Map<String, Object> caseData, String collection) {

        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return mapper.convertValue(caseData.get(collection),
            new TypeReference<List<ContestedUploadedDocumentData>>() {
            });
    }
}
