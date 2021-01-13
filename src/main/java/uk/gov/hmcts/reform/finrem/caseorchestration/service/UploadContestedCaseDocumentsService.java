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

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION;
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
    private final FeatureToggleService featureToggleService;

    public Map<String, Object> filterDocumentsToRelevantParty(Map<String, Object> caseData) {

        boolean respondentJourneyEnabled = featureToggleService.isRespondentJourneyEnabled();
        log.info("Respondent Solicitor Journey toggle is: {}", respondentJourneyEnabled);

        List<ContestedUploadedDocumentData> uploadedDocuments = getDocumentCollection(caseData, CONTESTED_UPLOADED_DOCUMENTS);

        if (respondentJourneyEnabled) {
            filterConfidentialDocs(uploadedDocuments, caseData, APPLICANT_CONFIDENTIAL_DOCS_COLLECTION, APPLICANT);
        }
        filterCorrespondence(uploadedDocuments, caseData, APPLICANT_CORRESPONDENCE_COLLECTION, APPLICANT);
        filterForms(uploadedDocuments, caseData, APPLICANT_FR_FORM_COLLECTION, APPLICANT);
        filterEvidence(uploadedDocuments, caseData, APPLICANT_EVIDENCE_COLLECTION, APPLICANT);
        filterTrialBundle(uploadedDocuments, caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION, APPLICANT);

        if (respondentJourneyEnabled) {
            filterConfidentialDocs(uploadedDocuments, caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION, RESPONDENT);
        }
        filterCorrespondence(uploadedDocuments, caseData, RESPONDENT_CORRESPONDENCE_COLLECTION, RESPONDENT);
        filterForms(uploadedDocuments, caseData, RESPONDENT_FR_FORM_COLLECTION, RESPONDENT);
        filterEvidence(uploadedDocuments, caseData, RESPONDENT_EVIDENCE_COLLECTION, RESPONDENT);
        filterTrialBundle(uploadedDocuments, caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION, RESPONDENT);

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

    private void filterCorrespondence(List<ContestedUploadedDocumentData> uploadedDocuments,
                                      Map<String, Object> caseData,
                                      String collection,
                                      String party) {
        List<ContestedUploadedDocumentData> correspondenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForCorrespondence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> correspondenceCollection = getDocumentCollection(caseData, collection);
        correspondenceCollection.addAll(correspondenceFiltered);
        log.info("Adding items: {}, to Correspondence Collection", correspondenceFiltered);
        uploadedDocuments.removeAll(correspondenceFiltered);

        if (!correspondenceCollection.isEmpty()) {
            caseData.put(collection, correspondenceCollection);
        }
    }

    private void filterForms(List<ContestedUploadedDocumentData> uploadedDocuments,
                             Map<String, Object> caseData,
                             String collection,
                             String party) {
        List<ContestedUploadedDocumentData> frFormsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForForms(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> formCollection = getDocumentCollection(caseData, collection);
        formCollection.addAll(frFormsFiltered);
        log.info("Adding items: {}, to FR Forms Collection", frFormsFiltered);
        uploadedDocuments.removeAll(frFormsFiltered);

        if (!formCollection.isEmpty()) {
            caseData.put(collection, formCollection);
        }
    }

    private void filterEvidence(List<ContestedUploadedDocumentData> uploadedDocuments,
                                Map<String, Object> caseData,
                                String collection,
                                String party) {
        List<ContestedUploadedDocumentData> evidenceInSupportFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForEvidence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> evidenceCollection = getDocumentCollection(caseData, collection);
        evidenceCollection.addAll(evidenceInSupportFiltered);
        log.info("Adding items: {}, to Evidence In Support Collection", evidenceInSupportFiltered);
        uploadedDocuments.removeAll(evidenceInSupportFiltered);

        if (!evidenceCollection.isEmpty()) {
            caseData.put(collection, evidenceCollection);
        }
    }

    private void filterTrialBundle(List<ContestedUploadedDocumentData> uploadedDocuments,
                                   Map<String, Object> caseData,
                                   String collection,
                                   String party) {
        List<ContestedUploadedDocumentData> trialBundleFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForTrialBundle(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> trialBundleCollection = getDocumentCollection(caseData, collection);
        trialBundleCollection.addAll(trialBundleFiltered);
        log.info("Adding items: {}, to Trial Bundle Collection", trialBundleFiltered);
        uploadedDocuments.removeAll(trialBundleFiltered);

        if (!trialBundleCollection.isEmpty()) {
            caseData.put(collection, trialBundleCollection);
        }
    }

    private void filterConfidentialDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                                        Map<String, Object> caseData,
                                        String collection,
                                        String party) {

        log.info("UploadDocuments Collection: {}", uploadedDocuments);
        List<ContestedUploadedDocumentData> confidentialFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential() != null
                && d.getUploadedCaseDocument().getCaseDocumentConfidential().equalsIgnoreCase("Yes"))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> confidentialDocsCollection = getDocumentCollection(caseData, collection);
        confidentialDocsCollection.addAll(confidentialFiltered);
        log.info("Adding items: {}, to Confidential Documents Collection", confidentialFiltered);
        uploadedDocuments.removeAll(confidentialFiltered);

        if (!confidentialDocsCollection.isEmpty()) {
            caseData.put(collection, confidentialDocsCollection);
        }
    }
}
