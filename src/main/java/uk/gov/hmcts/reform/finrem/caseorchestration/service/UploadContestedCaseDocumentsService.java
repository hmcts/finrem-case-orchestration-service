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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORMS_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_STATEMENTS_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_FR_FORM_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_TRIAL_BUNDLE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CASE_SUMMARIES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_CORRESPONDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_EXPERT_EVIDENCE_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_E_EXHIBITS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_FORM_H_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_HEARING_BUNDLES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_OTHER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_STATEMENTS_EXHIBITS_COLLECTION;


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
            filterHearingBundles(uploadedDocuments, caseData, APP_HEARING_BUNDLES_COLLECTION, APPLICANT);
            filterFormEExhibits(uploadedDocuments, caseData, APP_FORM_E_EXHIBITS_COLLECTION, APPLICANT);
            filterChronologiesStatements(uploadedDocuments, caseData, APP_CHRONOLOGIES_STATEMENTS_COLLECTION, APPLICANT);
            filterQuestionnairesAnswers(uploadedDocuments, caseData, APP_QUESTIONNAIRES_ANSWERS_COLLECTION, APPLICANT);
            filterStatementsExhibits(uploadedDocuments, caseData, APP_STATEMENTS_EXHIBITS_COLLECTION, APPLICANT);
            filterCaseSummaries(uploadedDocuments, caseData, APP_CASE_SUMMARIES_COLLECTION, APPLICANT);
            filterFormsH(uploadedDocuments, caseData, APP_FORMS_H_COLLECTION, APPLICANT);
            filterExpertEvidence(uploadedDocuments, caseData, APP_EXPERT_EVIDENCE_COLLECTION, APPLICANT);
            filterCorrespondenceDocs(uploadedDocuments, caseData, APP_CORRESPONDENCE_COLLECTION, APPLICANT);
            filterOtherDocs(uploadedDocuments, caseData, APP_OTHER_COLLECTION, APPLICANT);

        } else {
            filterCorrespondence(uploadedDocuments, caseData, APPLICANT_CORRESPONDENCE_COLLECTION, APPLICANT);
            filterForms(uploadedDocuments, caseData, APPLICANT_FR_FORM_COLLECTION, APPLICANT);
            filterEvidence(uploadedDocuments, caseData, APPLICANT_EVIDENCE_COLLECTION, APPLICANT);
            filterTrialBundle(uploadedDocuments, caseData, APPLICANT_TRIAL_BUNDLE_COLLECTION, APPLICANT);
        }
        if (respondentJourneyEnabled) {
            filterConfidentialDocs(uploadedDocuments, caseData, RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION, RESPONDENT);
            filterHearingBundles(uploadedDocuments, caseData, RESP_HEARING_BUNDLES_COLLECTION, RESPONDENT);
            filterFormEExhibits(uploadedDocuments, caseData, RESP_FORM_E_EXHIBITS_COLLECTION, RESPONDENT);
            filterChronologiesStatements(uploadedDocuments, caseData, RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, RESPONDENT);
            filterQuestionnairesAnswers(uploadedDocuments, caseData, RESP_QUESTIONNAIRES_ANSWERS_COLLECTION, RESPONDENT);
            filterStatementsExhibits(uploadedDocuments, caseData, RESP_STATEMENTS_EXHIBITS_COLLECTION, RESPONDENT);
            filterCaseSummaries(uploadedDocuments, caseData, RESP_CASE_SUMMARIES_COLLECTION, RESPONDENT);
            filterFormsH(uploadedDocuments, caseData, RESP_FORM_H_COLLECTION, RESPONDENT);
            filterExpertEvidence(uploadedDocuments, caseData, RESP_EXPERT_EVIDENCE_COLLECTION, RESPONDENT);
            filterCorrespondenceDocs(uploadedDocuments, caseData, RESP_CORRESPONDENCE_COLLECTION, RESPONDENT);
            filterOtherDocs(uploadedDocuments, caseData, RESP_OTHER_COLLECTION, RESPONDENT);
        } else {
            filterCorrespondence(uploadedDocuments, caseData, RESPONDENT_CORRESPONDENCE_COLLECTION, RESPONDENT);
            filterForms(uploadedDocuments, caseData, RESPONDENT_FR_FORM_COLLECTION, RESPONDENT);
            filterEvidence(uploadedDocuments, caseData, RESPONDENT_EVIDENCE_COLLECTION, RESPONDENT);
            filterTrialBundle(uploadedDocuments, caseData, RESPONDENT_TRIAL_BUNDLE_COLLECTION, RESPONDENT);
        }
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

    private boolean isTypeValidForHearingBundle(String caseDocumentType) {
        return caseDocumentType.equals("Trial Bundle");
    }

    private boolean isTypeValidForFormEExhibits(String caseDocumentType) {
        return caseDocumentType.equals("Applicant - Form E");
    }

    private boolean isTypeValidForChronologiesStatements(String caseDocumentType) {
        return caseDocumentType.equals("Statement of Issues")
            || caseDocumentType.equals("Chronology")
            || caseDocumentType.equals("Form G");
    }

    private boolean isTypeValidForQuestionnairesAnswers(String caseDocumentType) {
        return caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire");
    }

    private boolean isTypeValidForStatementsExhibits(String caseDocumentType) {
        return caseDocumentType.equals("Statement/Affidavit")
            || caseDocumentType.equals("Witness statement/Affidavit");
    }

    private boolean isTypeValidForCaseSummaries(String caseDocumentType) {
        return caseDocumentType.equals("Position Statement")
            || caseDocumentType.equals("Skeleton Argument")
            || caseDocumentType.equals("Case Summary");
    }

    private boolean isTypeValidForFormsH(String caseDocumentType) {
        return caseDocumentType.equals("Form H");
    }

    private boolean isTypeValidForExpertEvidence(String caseDocumentType) {
        return caseDocumentType.equals("Valuation Report")
            || caseDocumentType.equals("Expert Evidence");
    }

    private boolean isTypeValidForCorrespondenceDocs(String caseDocumentType) {
        return caseDocumentType.equals("Offers")
            || caseDocumentType.equals("Letter from Applicant");
    }

    private boolean isTypeValidForOtherDocs(String caseDocumentType) {
        return caseDocumentType.equals("other")
            || caseDocumentType.equals("Form B")
            || caseDocumentType.equals("Form F")
            || caseDocumentType.equals("Care Plan")
            || caseDocumentType.equals("Pension Plan");
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

    private void filterHearingBundles(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> hearingBundlesFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForHearingBundle(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> hearingBundlesCollection = getDocumentCollection(caseData, collection);
        hearingBundlesCollection.addAll(hearingBundlesFiltered);
        log.info("Adding items: {}, to Hearing Bundles Collection", hearingBundlesFiltered);
        uploadedDocuments.removeAll(hearingBundlesFiltered);

        if (!hearingBundlesCollection.isEmpty()) {
            caseData.put(collection, hearingBundlesCollection);
        }

    }

    private void filterFormEExhibits(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> formEExhibitsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForFormEExhibits(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> formEExhibitsCollection = getDocumentCollection(caseData, collection);
        formEExhibitsCollection.addAll(formEExhibitsFiltered);
        log.info("Adding items: {}, to Forms E & Exhibits Collection", formEExhibitsFiltered);
        uploadedDocuments.removeAll(formEExhibitsFiltered);

        if (!formEExhibitsCollection.isEmpty()) {
            caseData.put(collection, formEExhibitsCollection);
        }
    }

    private void filterChronologiesStatements(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> chronologiesStatementsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForChronologiesStatements(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> chronologiesStatementsCollection = getDocumentCollection(caseData, collection);
        chronologiesStatementsCollection.addAll(chronologiesStatementsFiltered);
        log.info("Adding items: {}, to Chronologies and Statements of Issues Collection", chronologiesStatementsFiltered);
        uploadedDocuments.removeAll(chronologiesStatementsFiltered);

        if (!chronologiesStatementsCollection.isEmpty()) {
            caseData.put(collection, chronologiesStatementsCollection);
        }
    }

    private void filterQuestionnairesAnswers(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> questionnairesAnswersFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForQuestionnairesAnswers(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> questionnairesAnswersCollection = getDocumentCollection(caseData, collection);
        questionnairesAnswersCollection.addAll(questionnairesAnswersFiltered);
        log.info("Adding items: {}, to Questionnaires & Answers to Questionnaires & Exhibits Collection", questionnairesAnswersFiltered);
        uploadedDocuments.removeAll(questionnairesAnswersFiltered);

        if (!questionnairesAnswersCollection.isEmpty()) {
            caseData.put(collection, questionnairesAnswersCollection);
        }
    }

    private void filterStatementsExhibits(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> statementsExhibitsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForStatementsExhibits(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> statementsExhibitsCollection = getDocumentCollection(caseData, collection);
        statementsExhibitsCollection.addAll(statementsExhibitsFiltered);
        log.info("Adding items: {}, to Statements & Exhibits Collection", statementsExhibitsFiltered);
        uploadedDocuments.removeAll(statementsExhibitsFiltered);

        if (!statementsExhibitsCollection.isEmpty()) {
            caseData.put(collection, statementsExhibitsCollection);
        }
    }

    private void filterCaseSummaries(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> caseSummariesFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForCaseSummaries(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> caseSummariesCollection = getDocumentCollection(caseData, collection);
        caseSummariesCollection.addAll(caseSummariesFiltered);
        log.info("Adding items: {}, to Case Summaries Collection", caseSummariesFiltered);
        uploadedDocuments.removeAll(caseSummariesFiltered);

        if (!caseSummariesCollection.isEmpty()) {
            caseData.put(collection, caseSummariesCollection);
        }
    }

    private void filterFormsH(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> formsHFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForFormsH(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> formsHCollection = getDocumentCollection(caseData, collection);
        formsHCollection.addAll(formsHFiltered);
        log.info("Adding items: {}, to Forms H Collection", formsHFiltered);
        uploadedDocuments.removeAll(formsHFiltered);

        if (!formsHCollection.isEmpty()) {
            caseData.put(collection, formsHCollection);
        }
    }

    private void filterExpertEvidence(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> expertEvidenceFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForExpertEvidence(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> expertEvidenceCollection = getDocumentCollection(caseData, collection);
        expertEvidenceCollection.addAll(expertEvidenceFiltered);
        log.info("Adding items: {}, to Expert Evidence Collection", expertEvidenceFiltered);
        uploadedDocuments.removeAll(expertEvidenceFiltered);

        if (!expertEvidenceCollection.isEmpty()) {
            caseData.put(collection, expertEvidenceCollection);
        }
    }

    private void filterCorrespondenceDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> correspondenceDocsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForCorrespondenceDocs(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> correspondenceCollection = getDocumentCollection(caseData, collection);
        correspondenceCollection.addAll(correspondenceDocsFiltered);
        log.info("Adding items: {}, to Correspondence Docs Collection", correspondenceDocsFiltered);
        uploadedDocuments.removeAll(correspondenceDocsFiltered);

        if (!correspondenceCollection.isEmpty()) {
            caseData.put(collection, correspondenceCollection);
        }
    }

    private void filterOtherDocs(List<ContestedUploadedDocumentData> uploadedDocuments,
                        Map<String, Object> caseData,
                        String collection,
                        String party) {
        List<ContestedUploadedDocumentData> otherDocsFiltered = uploadedDocuments.stream()
            .filter(d -> d.getUploadedCaseDocument().getCaseDocuments() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty() != null
                && d.getUploadedCaseDocument().getCaseDocumentParty().equals(party))
            .filter(d -> d.getUploadedCaseDocument().getCaseDocumentType() != null
                && isTypeValidForOtherDocs(d.getUploadedCaseDocument().getCaseDocumentType()))
            .collect(Collectors.toList());

        List<ContestedUploadedDocumentData> otherCollection = getDocumentCollection(caseData, collection);
        otherCollection.addAll(otherDocsFiltered);
        log.info("Adding items: {}, to Other Docs Collection", otherDocsFiltered);
        uploadedDocuments.removeAll(otherDocsFiltered);

        if (!otherCollection.isEmpty()) {
            caseData.put(collection, otherCollection);
        }
    }
}
