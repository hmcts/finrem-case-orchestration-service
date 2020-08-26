package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_TRIAL_BUNDLE_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsService {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";

    private final ObjectMapper mapper;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) throws Exception {

        List<ContestedUploadedDocumentData> uploadedDocuments = getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS);

        filterApplicantCorrespondence(uploadedDocuments, caseData);
        filterApplicantForms(uploadedDocuments, caseData);
        filterApplicantEvidence(uploadedDocuments, caseData);
        filterApplicantTrialBundle(uploadedDocuments, caseData);

        filterRespondentCorrespondence(uploadedDocuments, caseData);
        filterRespondentForms(uploadedDocuments, caseData);
        filterRespondentEvidence(uploadedDocuments, caseData);
        filterRespondentTrialBundle(uploadedDocuments, caseData);

        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);

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

    private boolean isTypeValidForCorrespondence(String caseDocumentType) {
        return caseDocumentType.equals("Letter from Applicant");
    }

    private boolean isTypeValidForForms(String caseDocumentType) {
        return caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Applicant - Form E")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Form G")
            || caseDocumentType.equals("Form H");
    }

    private boolean isTypeValidForEvidence(String caseDocumentType) {
        return caseDocumentType.equals("Statement of Issues")
            || caseDocumentType.equals("Chronology")
            || caseDocumentType.equals("Case Summary")
            || caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire")
            || caseDocumentType.equals("Valuation Report")
            || caseDocumentType.equals("Pension Plan")
            || caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Expert Evidence")
            || caseDocumentType.equals("Witness Statement/Affidavit")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Offers")
            || caseDocumentType.equals("other");
    }

    private boolean isTypeValidForTrialBundle(String caseDocumentType) {
        return caseDocumentType.equals("Trial Bundle");
    }

    private void filterApplicantCorrespondence(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> correspondenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> isTypeValidForCorrespondence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantCorrespondenceCollection = getDocumentCollection(caseData, APPLICANT_CORRESPONDENCE_COLLECTION);

        applicantCorrespondenceCollection.addAll(correspondenceFiltered);
        log.info("Adding items: {}, to Applicant Correspondence Collection", correspondenceFiltered);

        uploadedDocuments.removeAll(correspondenceFiltered);

        caseData.put(APPLICANT_CORRESPONDENCE_COLLECTION, applicantCorrespondenceCollection);
    }

    private void filterApplicantForms(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> frFormsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> isTypeValidForForms(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantFormCollection = getDocumentCollection(caseData, APPLICANT_FR_FORM_COLLECTION);
        applicantFormCollection.addAll(frFormsFiltered);
        log.info("Adding items: {}, to Applicant FR Forms Collection", frFormsFiltered);

        uploadedDocuments.removeAll(frFormsFiltered);

        caseData.put(APPLICANT_FR_FORM_COLLECTION, applicantFormCollection);
    }

    private void filterApplicantEvidence(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> evidenceInSupportFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> isTypeValidForEvidence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantEvidenceCollection = getDocumentCollection(caseData, APPLICANT_EVIDENCE_COLLECTION);
        applicantEvidenceCollection.addAll(evidenceInSupportFiltered);
        log.info("Adding items: {}, to Applicant Evidence In Support Collection", evidenceInSupportFiltered);

        uploadedDocuments.removeAll(evidenceInSupportFiltered);

        caseData.put(APPLICANT_EVIDENCE_COLLECTION, applicantEvidenceCollection);
    }

    private void filterApplicantTrialBundle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> trialBundleFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(APPLICANT))
            .filter(d -> isTypeValidForTrialBundle(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> applicantTrialBundleCollection = getDocumentCollection(caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION);
        applicantTrialBundleCollection.addAll(trialBundleFiltered);
        log.info("Adding items: {}, to Applicant Trial Bundle Collection", trialBundleFiltered);

        uploadedDocuments.removeAll(trialBundleFiltered);

        caseData.put(APPLICANT_TRIAL_BUNDLE_COLLECTION, applicantTrialBundleCollection);
    }

    private void filterRespondentCorrespondence(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> respondentCorrespondenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(RESPONDENT))
            .filter(d -> isTypeValidForCorrespondence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> respondentCorrespondenceCollection =
            getDocumentCollection(caseData, RESPONDENT_CORRESPONDENCE_COLLECTION);

        respondentCorrespondenceCollection.addAll(respondentCorrespondenceFiltered);
        log.info("Adding items: {}, to Applicant Correspondence Collection", respondentCorrespondenceFiltered);

        uploadedDocuments.removeAll(respondentCorrespondenceFiltered);

        caseData.put(RESPONDENT_CORRESPONDENCE_COLLECTION, respondentCorrespondenceCollection);
    }

    private void filterRespondentForms(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> frFormsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(RESPONDENT))
            .filter(d -> isTypeValidForForms(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> respondentFormCollection = getDocumentCollection(caseData, RESPONDENT_FR_FORM_COLLECTION);
        respondentFormCollection.addAll(frFormsFiltered);
        log.info("Adding items: {}, to Applicant FR Forms Collection", frFormsFiltered);

        uploadedDocuments.removeAll(frFormsFiltered);

        caseData.put(RESPONDENT_FR_FORM_COLLECTION, respondentFormCollection);
    }

    private void filterRespondentEvidence(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> evidenceInSupportFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(RESPONDENT))
            .filter(d -> isTypeValidForEvidence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> respondentEvidenceCollection = getDocumentCollection(caseData, RESPONDENT_EVIDENCE_COLLECTION);
        respondentEvidenceCollection.addAll(evidenceInSupportFiltered);
        log.info("Adding items: {}, to Applicant Evidence In Support Collection", evidenceInSupportFiltered);

        uploadedDocuments.removeAll(evidenceInSupportFiltered);

        caseData.put(RESPONDENT_EVIDENCE_COLLECTION, respondentEvidenceCollection);
    }

    private void filterRespondentTrialBundle(List<ContestedUploadedDocumentData> uploadedDocuments, Map<String, Object> caseData) {
        List<ContestedUploadedDocumentData> trialBundleFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(RESPONDENT))
            .filter(d -> isTypeValidForTrialBundle(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());
        List<ContestedUploadedDocumentData> respondentTrialBundleCollection = getDocumentCollection(caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION);
        respondentTrialBundleCollection.addAll(trialBundleFiltered);
        log.info("Adding items: {}, to Applicant Trial Bundle Collection", trialBundleFiltered);

        uploadedDocuments.removeAll(trialBundleFiltered);

        caseData.put(RESPONDENT_TRIAL_BUNDLE_COLLECTION, respondentTrialBundleCollection);
    }
}
