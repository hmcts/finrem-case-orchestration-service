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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            .filter(Objects::nonNull)
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UploadCaseDocumentCollection> getDocumentCollection(ManageCaseDocumentsCollectionType collectionName) {

        return switch (collectionName) {
            case APPLICANT_FR_FORM_COLLECTION ->
                appFrFormsCollection = getNonNull(appFrFormsCollection);
            case APPLICANT_EVIDENCE_COLLECTION ->
                appEvidenceCollection = getNonNull(appEvidenceCollection);
            case APPLICANT_TRIAL_BUNDLE_COLLECTION ->
                appTrialBundleCollection = getNonNull(appTrialBundleCollection);
            case APPLICANT_CONFIDENTIAL_DOCS_COLLECTION ->
                appConfidentialDocsCollection = getNonNull(appConfidentialDocsCollection);
            case RESPONDENT_FR_FORM_COLLECTION ->
                respFrFormsCollection = getNonNull(respFrFormsCollection);
            case RESPONDENT_EVIDENCE_COLLECTION ->
                respEvidenceCollection = getNonNull(respEvidenceCollection);
            case RESPONDENT_TRIAL_BUNDLE_COLLECTION ->
                respTrialBundleCollection = getNonNull(respTrialBundleCollection);
            case RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION ->
                respConfidentialDocsCollection = getNonNull(respConfidentialDocsCollection);
            case APP_HEARING_BUNDLES_COLLECTION ->
                appHearingBundlesCollection = getNonNull(appHearingBundlesCollection);
            case APP_FORM_E_EXHIBITS_COLLECTION ->
                appFormEExhibitsCollection = getNonNull(appFormEExhibitsCollection);
            case APP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                appChronologiesCollection = getNonNull(appChronologiesCollection);
            case APP_QUESTIONNAIRES_ANSWERS_COLLECTION ->
                appQaCollection = getNonNull(appQaCollection);
            case APP_STATEMENTS_EXHIBITS_COLLECTION ->
                appStatementsExhibitsCollection = getNonNull(appStatementsExhibitsCollection);
            case APP_CASE_SUMMARIES_COLLECTION ->
                appCaseSummariesCollection = getNonNull(appCaseSummariesCollection);
            case APP_FORMS_H_COLLECTION ->
                appFormsHCollection = getNonNull(appFormsHCollection);
            case APP_EXPERT_EVIDENCE_COLLECTION ->
                appExpertEvidenceCollection = getNonNull(appExpertEvidenceCollection);
            case APP_CORRESPONDENCE_COLLECTION ->
                appCorrespondenceDocsCollection = getNonNull(appCorrespondenceDocsCollection);
            case APP_OTHER_COLLECTION ->
                appOtherCollection = getNonNull(appOtherCollection);
            case RESP_HEARING_BUNDLES_COLLECTION ->
                respHearingBundlesCollection = getNonNull(respHearingBundlesCollection);
            case RESP_FORM_E_EXHIBITS_COLLECTION ->
                respFormEExhibitsCollection = getNonNull(respFormEExhibitsCollection);
            case RESP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                respChronologiesCollection = getNonNull(respChronologiesCollection);
            case RESP_QUESTIONNAIRES_ANSWERS_COLLECTION ->
                respQaCollection = getNonNull(respQaCollection);
            case RESP_STATEMENTS_EXHIBITS_COLLECTION ->
                respStatementsExhibitsCollection = getNonNull(respStatementsExhibitsCollection);
            case RESP_CASE_SUMMARIES_COLLECTION ->
                respCaseSummariesCollection = getNonNull(respCaseSummariesCollection);
            case RESP_FORM_H_COLLECTION ->
                respFormsHCollection = getNonNull(respFormsHCollection);
            case RESP_EXPERT_EVIDENCE_COLLECTION ->
                respExpertEvidenceCollection = getNonNull(respExpertEvidenceCollection);
            case RESP_CORRESPONDENCE_COLLECTION ->
                respCorrespondenceDocsColl = getNonNull(respCorrespondenceDocsColl);
            case RESP_OTHER_COLLECTION ->
                respOtherCollection = getNonNull(respOtherCollection);
            case CONTESTED_UPLOADED_DOCUMENTS ->
                uploadCaseDocument = getNonNull(uploadCaseDocument);
            case CONTESTED_FDR_CASE_DOCUMENT_COLLECTION ->
                fdrCaseDocumentCollection = getNonNull(fdrCaseDocumentCollection);
        };
    }

    private List<UploadCaseDocumentCollection> getNonNull(List<UploadCaseDocumentCollection> collection) {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        return collection;
    }
}

