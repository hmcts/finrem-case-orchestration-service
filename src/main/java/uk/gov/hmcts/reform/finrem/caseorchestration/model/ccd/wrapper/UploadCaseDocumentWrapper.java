package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * When adding new collections here please update the getAll method.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadCaseDocumentWrapper {
    private List<UploadCaseDocumentCollection> uploadCaseDocument;
    private List<UploadCaseDocumentCollection> fdrCaseDocumentCollection;
    private List<UploadCaseDocumentCollection> appCorrespondenceCollection;
    @JsonProperty("appFRFormsCollection")
    private List<UploadCaseDocumentCollection> appFrFormsCollection;
    private List<UploadCaseDocumentCollection> appEvidenceCollection;
    private List<UploadCaseDocumentCollection> appTrialBundleCollection;
    private List<UploadCaseDocumentCollection> appConfidentialDocsCollection;
    private List<UploadCaseDocumentCollection> respCorrespondenceCollection;
    @JsonProperty("respFRFormsCollection")
    private List<UploadCaseDocumentCollection> respFrFormsCollection;
    private List<UploadCaseDocumentCollection> respEvidenceCollection;
    private List<UploadCaseDocumentCollection> respTrialBundleCollection;
    private List<UploadCaseDocumentCollection> respConfidentialDocsCollection;
    private List<UploadCaseDocumentCollection> appHearingBundlesCollection;
    private List<UploadCaseDocumentCollection> appFormEExhibitsCollection;
    private List<UploadCaseDocumentCollection> appChronologiesCollection;
    @JsonProperty("appQACollection")
    private List<UploadCaseDocumentCollection> appQaCollection;
    private List<UploadCaseDocumentCollection> appStatementsExhibitsCollection;
    private List<UploadCaseDocumentCollection> appCaseSummariesCollection;
    private List<UploadCaseDocumentCollection> appFormsHCollection;
    private List<UploadCaseDocumentCollection> appExpertEvidenceCollection;
    private List<UploadCaseDocumentCollection> appCorrespondenceDocsCollection;
    private List<UploadCaseDocumentCollection> appOtherCollection;
    private List<UploadCaseDocumentCollection> respHearingBundlesCollection;
    private List<UploadCaseDocumentCollection> respFormEExhibitsCollection;
    private List<UploadCaseDocumentCollection> respChronologiesCollection;
    @JsonProperty("respQACollection")
    private List<UploadCaseDocumentCollection> respQaCollection;
    private List<UploadCaseDocumentCollection> respStatementsExhibitsCollection;
    private List<UploadCaseDocumentCollection> respCaseSummariesCollection;
    private List<UploadCaseDocumentCollection> respFormsHCollection;
    private List<UploadCaseDocumentCollection> respExpertEvidenceCollection;
    private List<UploadCaseDocumentCollection> respCorrespondenceDocsColl;
    private List<UploadCaseDocumentCollection> respOtherCollection;
    private List<UploadCaseDocumentCollection> appHearingBundlesCollectionShared;
    private List<UploadCaseDocumentCollection> appFormEExhibitsCollectionShared;
    private List<UploadCaseDocumentCollection> appChronologiesCollectionShared;
    @JsonProperty("appQACollectionShared")
    private List<UploadCaseDocumentCollection> appQaCollectionShared;
    private List<UploadCaseDocumentCollection> appStatementsExhibitsCollShared;
    private List<UploadCaseDocumentCollection> appCaseSummariesCollectionShared;
    private List<UploadCaseDocumentCollection> appFormsHCollectionShared;
    private List<UploadCaseDocumentCollection> appExpertEvidenceCollectionShared;
    private List<UploadCaseDocumentCollection> appCorrespondenceDocsCollShared;
    private List<UploadCaseDocumentCollection> appOtherCollectionShared;
    private List<UploadCaseDocumentCollection> respHearingBundlesCollShared;
    private List<UploadCaseDocumentCollection> respFormEExhibitsCollectionShared;
    private List<UploadCaseDocumentCollection> respChronologiesCollectionShared;
    @JsonProperty("respQACollectionShared")
    private List<UploadCaseDocumentCollection> respQaCollectionShared;
    private List<UploadCaseDocumentCollection> respStatementsExhibitsCollShared;
    private List<UploadCaseDocumentCollection> respCaseSummariesCollectionShared;
    private List<UploadCaseDocumentCollection> respFormsHCollectionShared;
    private List<UploadCaseDocumentCollection> respExpertEvidenceCollShared;
    private List<UploadCaseDocumentCollection> respCorrespondenceDocsCollShared;
    private List<UploadCaseDocumentCollection> respOtherCollectionShared;


    @JsonIgnore
    public List<UploadCaseDocumentCollection> getAllCollections() {
        return Stream.of(uploadCaseDocument, fdrCaseDocumentCollection, appCorrespondenceCollection,
                appFrFormsCollection, appEvidenceCollection, appTrialBundleCollection, appConfidentialDocsCollection,
                respCorrespondenceCollection, respFrFormsCollection, respEvidenceCollection, respTrialBundleCollection,
                respConfidentialDocsCollection, appHearingBundlesCollection, appFormEExhibitsCollection,
                appChronologiesCollection, appQaCollection, appStatementsExhibitsCollection, appCaseSummariesCollection,
                appFormsHCollection, appExpertEvidenceCollection, appCorrespondenceDocsCollection, appOtherCollection,
                respHearingBundlesCollection, respFormEExhibitsCollection, respChronologiesCollection, respQaCollection,
                respStatementsExhibitsCollection, respCaseSummariesCollection, respFormsHCollection,
                respExpertEvidenceCollection, respCorrespondenceDocsColl, respOtherCollection,
                appHearingBundlesCollectionShared, appFormEExhibitsCollectionShared, appChronologiesCollectionShared,
                appQaCollectionShared, appStatementsExhibitsCollShared, appCaseSummariesCollectionShared,
                appFormsHCollectionShared, appExpertEvidenceCollectionShared, appCorrespondenceDocsCollShared,
                appOtherCollectionShared, respHearingBundlesCollShared, respFormEExhibitsCollectionShared,
                respChronologiesCollectionShared, respStatementsExhibitsCollShared, respQaCollectionShared,
                respCaseSummariesCollectionShared, respFormsHCollectionShared, respExpertEvidenceCollShared,
                respCorrespondenceDocsCollShared, respOtherCollectionShared)
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UploadCaseDocumentCollection> getDocumentCollection(ManageCaseDocumentsCollectionType collectionName) {

        return switch (collectionName) {
            case APPLICANT_CORRESPONDENCE_COLLECTION -> getAppCorrespondenceCollection();
            case APPLICANT_FR_FORM_COLLECTION -> getAppFrFormsCollection();
            case APPLICANT_EVIDENCE_COLLECTION -> getAppEvidenceCollection();
            case APPLICANT_TRIAL_BUNDLE_COLLECTION -> getAppTrialBundleCollection();
            case APPLICANT_CONFIDENTIAL_DOCS_COLLECTION -> getAppConfidentialDocsCollection();
            case RESPONDENT_CORRESPONDENCE_COLLECTION -> getRespCorrespondenceCollection();
            case RESPONDENT_FR_FORM_COLLECTION -> getRespFrFormsCollection();
            case RESPONDENT_EVIDENCE_COLLECTION -> getRespEvidenceCollection();
            case RESPONDENT_TRIAL_BUNDLE_COLLECTION -> getRespTrialBundleCollection();
            case RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION -> getRespConfidentialDocsCollection();
            case APP_HEARING_BUNDLES_COLLECTION -> getAppHearingBundlesCollection();
            case APP_FORM_E_EXHIBITS_COLLECTION -> getAppFormEExhibitsCollection();
            case APP_CHRONOLOGIES_STATEMENTS_COLLECTION -> getAppChronologiesCollection();
            case APP_QUESTIONNAIRES_ANSWERS_COLLECTION -> getAppQaCollection();
            case APP_STATEMENTS_EXHIBITS_COLLECTION -> getAppStatementsExhibitsCollection();
            case APP_CASE_SUMMARIES_COLLECTION -> getAppCaseSummariesCollection();
            case APP_FORMS_H_COLLECTION -> getAppFormsHCollection();
            case APP_EXPERT_EVIDENCE_COLLECTION -> getAppExpertEvidenceCollection();
            case APP_CORRESPONDENCE_COLLECTION -> getAppCorrespondenceDocsCollection();
            case APP_OTHER_COLLECTION -> getAppOtherCollection();
            case RESP_HEARING_BUNDLES_COLLECTION -> getRespHearingBundlesCollection();
            case RESP_FORM_E_EXHIBITS_COLLECTION -> getRespFormEExhibitsCollection();
            case RESP_CHRONOLOGIES_STATEMENTS_COLLECTION -> getRespChronologiesCollection();
            case RESP_QUESTIONNAIRES_ANSWERS_COLLECTION -> getRespQaCollection();
            case RESP_STATEMENTS_EXHIBITS_COLLECTION -> getRespStatementsExhibitsCollection();
            case RESP_CASE_SUMMARIES_COLLECTION -> getRespCaseSummariesCollection();
            case RESP_FORM_H_COLLECTION -> getRespFormsHCollection();
            case RESP_EXPERT_EVIDENCE_COLLECTION -> getRespExpertEvidenceCollection();
            case RESP_CORRESPONDENCE_COLLECTION -> getRespCorrespondenceDocsColl();
            case RESP_OTHER_COLLECTION -> getRespOtherCollection();
            case CONTESTED_UPLOADED_DOCUMENTS -> getUploadCaseDocument();
            case CONTESTED_FDR_CASE_DOCUMENT_COLLECTION -> getFdrCaseDocumentCollection();
        };
    }
}
