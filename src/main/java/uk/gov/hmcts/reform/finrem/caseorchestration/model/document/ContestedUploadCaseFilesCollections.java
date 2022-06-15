package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

public enum ContestedUploadCaseFilesCollections {

    APPLICANT_CORRESPONDENCE_COLLECTION("appCorrespondenceCollection"),
    APPLICANT_FR_FORM_COLLECTION("appFRFormsCollection"),
    APPLICANT_EVIDENCE_COLLECTION("appEvidenceCollection"),
    APPLICANT_TRIAL_BUNDLE_COLLECTION("appTrialBundleCollection"),
    APPLICANT_CONFIDENTIAL_DOCS_COLLECTION("appConfidentialDocsCollection"),
    RESPONDENT_CORRESPONDENCE_COLLECTION("respCorrespondenceCollection"),
    RESPONDENT_FR_FORM_COLLECTION("respFRFormsCollection"),
    RESPONDENT_EVIDENCE_COLLECTION("respEvidenceCollection"),
    RESPONDENT_TRIAL_BUNDLE_COLLECTION("respTrialBundleCollection"),
    RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION("respConfidentialDocsCollection"),
    APP_HEARING_BUNDLES_COLLECTION("appHearingBundlesCollection"),
    APP_FORM_E_EXHIBITS_COLLECTION("appFormEExhibitsCollection"),
    APP_CHRONOLOGIES_STATEMENTS_COLLECTION("appChronologiesCollection"),
    APP_QUESTIONNAIRES_ANSWERS_COLLECTION("appQACollection"),
    APP_STATEMENTS_EXHIBITS_COLLECTION("appStatementsExhibitsCollection"),
    APP_CASE_SUMMARIES_COLLECTION("appCaseSummariesCollection"),
    APP_FORMS_H_COLLECTION("appFormsHCollection"),
    APP_EXPERT_EVIDENCE_COLLECTION("appExpertEvidenceCollection"),
    APP_CORRESPONDENCE_COLLECTION("appCorrespondenceDocsColl"),
    APP_OTHER_COLLECTION("appOtherCollection"),
    RESP_HEARING_BUNDLES_COLLECTION("respHearingBundlesCollection"),
    RESP_FORM_E_EXHIBITS_COLLECTION("respFormEExhibitsCollection"),
    RESP_CHRONOLOGIES_STATEMENTS_COLLECTION("respChronologiesCollection"),
    RESP_QUESTIONNAIRES_ANSWERS_COLLECTION("respQACollection"),
    RESP_STATEMENTS_EXHIBITS_COLLECTION("respStatementsExhibitsCollection"),
    RESP_CASE_SUMMARIES_COLLECTION("respCaseSummariesCollection"),
    RESP_FORM_H_COLLECTION("respFormsHCollection"),
    RESP_EXPERT_EVIDENCE_COLLECTION("respExpertEvidenceCollection"),
    RESP_CORRESPONDENCE_COLLECTION("respCorrespondenceDocsColl"),
    RESP_OTHER_COLLECTION("respOtherCollection");

    ContestedUploadCaseFilesCollections(String value) {
        this.value = value;
    }

    String value;

    @Override
    public String toString() {
        return value;
    }
}
