package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.UploadCaseDocumentWrapper;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public enum ContestedUploadCaseFilesCollectionType {

    APPLICANT_CORRESPONDENCE_COLLECTION("appCorrespondenceCollection", getAppCorrespondenceField()),
    APPLICANT_FR_FORM_COLLECTION("appFRFormsCollection", getAppFrFormsCollectionField()),
    APPLICANT_EVIDENCE_COLLECTION("appEvidenceCollection", getAppEvidenceCollectionField()),
    APPLICANT_TRIAL_BUNDLE_COLLECTION("appTrialBundleCollection", getAppTrialBundleCollectionField()),
    APPLICANT_CONFIDENTIAL_DOCS_COLLECTION("appConfidentialDocsCollection", getAppConfidentialDocsCollectionField()),
    RESPONDENT_CORRESPONDENCE_COLLECTION("respCorrespondenceCollection", getRespCorrespondentCollectionField()),
    RESPONDENT_FR_FORM_COLLECTION("respFRFormsCollection", getRespFrFormsCollectionField()),
    RESPONDENT_EVIDENCE_COLLECTION("respEvidenceCollection", getRespEvidenceCollectionField()),
    RESPONDENT_TRIAL_BUNDLE_COLLECTION("respTrialBundleCollection", getRespTrialBundleCollectionField()),
    RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION("respConfidentialDocsCollection", getRespConfidentialDocsCollectionField()),
    APP_HEARING_BUNDLES_COLLECTION("appHearingBundlesCollection", getAppHearingBundlesCollectionField()),
    APP_FORM_E_EXHIBITS_COLLECTION("appFormEExhibitsCollection", getAppFormEExhibitsCollectionField()),
    APP_CHRONOLOGIES_STATEMENTS_COLLECTION("appChronologiesCollection", getAppChronologiesCollectionField()),
    APP_QUESTIONNAIRES_ANSWERS_COLLECTION("appQACollection", getAppQaCollectionField()),
    APP_STATEMENTS_EXHIBITS_COLLECTION("appStatementsExhibitsCollection", getAppStatementsExhibitsCollectionField()),
    APP_CASE_SUMMARIES_COLLECTION("appCaseSummariesCollection", getAppCaseSummariesCollectionField()),
    APP_FORMS_H_COLLECTION("appFormsHCollection", getAppFormsHCollectionField()),
    APP_EXPERT_EVIDENCE_COLLECTION("appExpertEvidenceCollection", getAppExpertEvidenceCollectionField()),
    APP_CORRESPONDENCE_COLLECTION("appCorrespondenceDocsColl", getAppCorrespondenceDocsCollectionField()),
    APP_OTHER_COLLECTION("appOtherCollection", getAppOtherCollectionField()),
    RESP_HEARING_BUNDLES_COLLECTION("respHearingBundlesCollection", getRespHearingBundlesCollectionField()),
    RESP_FORM_E_EXHIBITS_COLLECTION("respFormEExhibitsCollection", getRespFormEExhibitsCollectionField()),
    RESP_CHRONOLOGIES_STATEMENTS_COLLECTION("respChronologiesCollection", getRespChronologiesCollectionField()),
    RESP_QUESTIONNAIRES_ANSWERS_COLLECTION("respQACollection", getRespQaCollectionField()),
    RESP_STATEMENTS_EXHIBITS_COLLECTION("respStatementsExhibitsCollection", getRespStatementsExhibitsCollectionField()),
    RESP_CASE_SUMMARIES_COLLECTION("respCaseSummariesCollection", getRespCaseSummariesCollectionField()),
    RESP_FORM_H_COLLECTION("respFormsHCollection", getRespFormsHCollectionField()),
    RESP_EXPERT_EVIDENCE_COLLECTION("respExpertEvidenceCollection", getRespExpertEvidenceCollectionField()),
    RESP_CORRESPONDENCE_COLLECTION("respCorrespondenceDocsColl", getRespCorrespondenceDocsCollField()),
    RESP_OTHER_COLLECTION("respOtherCollection",getRespOtherCollectionField()),
    CONTESTED_UPLOADED_DOCUMENTS("uploadCaseDocument", getUploadCaseDocumentField()),
    CONTESTED_FDR_CASE_DOCUMENT_COLLECTION("fdrCaseDocumentCollection", getFdrCaseDocumentCollectionField());

    private final String ccdKey;

    private final Field manageCaseDocumentCollectionField;

    public String getCcdKey() {
        return ccdKey;
    }

    public Field getManageCaseDocumentCollectionField() {
        return manageCaseDocumentCollectionField;
    }

    private static Field getAppCorrespondenceField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appCorrespondenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppFrFormsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appFrFormsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppEvidenceCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appEvidenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppTrialBundleCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appTrialBundleCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppConfidentialDocsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appConfidentialDocsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespCorrespondentCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respCorrespondenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespFrFormsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respFrFormsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespEvidenceCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respEvidenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespTrialBundleCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respTrialBundleCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespConfidentialDocsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respConfidentialDocsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppHearingBundlesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appHearingBundlesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppFormEExhibitsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appFormEExhibitsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppChronologiesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appChronologiesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppQaCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appQaCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppStatementsExhibitsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appStatementsExhibitsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppCaseSummariesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appCaseSummariesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppFormsHCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appFormsHCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppExpertEvidenceCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appExpertEvidenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppCorrespondenceDocsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appCorrespondenceDocsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getAppOtherCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("appOtherCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespHearingBundlesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respHearingBundlesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespFormEExhibitsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respFormEExhibitsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespChronologiesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respChronologiesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespQaCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respQaCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespStatementsExhibitsCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respStatementsExhibitsCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespCaseSummariesCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respCaseSummariesCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespFormsHCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respFormsHCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespExpertEvidenceCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respExpertEvidenceCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespCorrespondenceDocsCollField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respCorrespondenceDocsColl");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getRespOtherCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("respOtherCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getUploadCaseDocumentField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("uploadCaseDocument");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }

    private static Field getFdrCaseDocumentCollectionField() {
        try {
            return UploadCaseDocumentWrapper.class.getDeclaredField("fdrCaseDocumentCollection");
        } catch (NoSuchFieldException nse) {
            return null;
        }
    }
}