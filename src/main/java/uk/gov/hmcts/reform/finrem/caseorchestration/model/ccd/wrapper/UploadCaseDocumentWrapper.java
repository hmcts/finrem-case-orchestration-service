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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

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

    private List<UploadCaseDocumentCollection> intv1Summaries;
    private List<UploadCaseDocumentCollection> intv1Chronologies;
    private List<UploadCaseDocumentCollection> intv1CorrespDocs;
    private List<UploadCaseDocumentCollection> intv1ExpertEvidence;
    private List<UploadCaseDocumentCollection> intv1FormEsExhibits;
    private List<UploadCaseDocumentCollection> intv1FormHs;
    private List<UploadCaseDocumentCollection> intv1HearingBundles;
    private List<UploadCaseDocumentCollection> intv1Other;
    private List<UploadCaseDocumentCollection> intv1Qa;
    private List<UploadCaseDocumentCollection> intv1StmtsExhibits;
    private List<UploadCaseDocumentCollection> intv2Summaries;
    private List<UploadCaseDocumentCollection> intv2Chronologies;
    private List<UploadCaseDocumentCollection> intv2CorrespDocs;
    private List<UploadCaseDocumentCollection> intv2ExpertEvidence;
    private List<UploadCaseDocumentCollection> intv2FormEsExhibits;
    private List<UploadCaseDocumentCollection> intv2FormHs;
    private List<UploadCaseDocumentCollection> intv2HearingBundles;
    private List<UploadCaseDocumentCollection> intv2Other;
    private List<UploadCaseDocumentCollection> intv2Qa;
    private List<UploadCaseDocumentCollection> intv2StmtsExhibits;
    private List<UploadCaseDocumentCollection> intv3Summaries;
    private List<UploadCaseDocumentCollection> intv3Chronologies;
    private List<UploadCaseDocumentCollection> intv3CorrespDocs;
    private List<UploadCaseDocumentCollection> intv3ExpertEvidence;
    private List<UploadCaseDocumentCollection> intv3FormEsExhibits;
    private List<UploadCaseDocumentCollection> intv3FormHs;
    private List<UploadCaseDocumentCollection> intv3HearingBundles;
    private List<UploadCaseDocumentCollection> intv3Other;
    private List<UploadCaseDocumentCollection> intv3Qa;
    private List<UploadCaseDocumentCollection> intv3StmtsExhibits;
    private List<UploadCaseDocumentCollection> intv4Summaries;
    private List<UploadCaseDocumentCollection> intv4Chronologies;
    private List<UploadCaseDocumentCollection> intv4CorrespDocs;
    private List<UploadCaseDocumentCollection> intv4ExpertEvidence;
    private List<UploadCaseDocumentCollection> intv4FormEsExhibits;
    private List<UploadCaseDocumentCollection> intv4FormHs;
    private List<UploadCaseDocumentCollection> intv4HearingBundles;
    private List<UploadCaseDocumentCollection> intv4Other;
    private List<UploadCaseDocumentCollection> intv4Qa;
    private List<UploadCaseDocumentCollection> intv4StmtsExhibits;
    private List<UploadCaseDocumentCollection> intv1FdrCaseDocuments;
    private List<UploadCaseDocumentCollection> intv2FdrCaseDocuments;
    private List<UploadCaseDocumentCollection> intv3FdrCaseDocuments;
    private List<UploadCaseDocumentCollection> intv4FdrCaseDocuments;
    private List<UploadCaseDocumentCollection> confidentialDocumentCollection;



    private List<UploadCaseDocumentCollection> intv1HearingBundlesShared;
    @JsonProperty("intv1FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv1FormEsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv1ChronologiesShared;
    @JsonProperty("intv1QAShared")
    private List<UploadCaseDocumentCollection> intv1QaShared;
    private List<UploadCaseDocumentCollection> intv1StmtsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv1SummariesShared;
    @JsonProperty("intv1FormsHShared")
    private List<UploadCaseDocumentCollection> intv1FormHsShared;
    private List<UploadCaseDocumentCollection> intv1ExpertEvidenceShared;
    private List<UploadCaseDocumentCollection> intv1CorrespDocsShared;
    private List<UploadCaseDocumentCollection> intv1OtherShared;

    private List<UploadCaseDocumentCollection> intv2HearingBundlesShared;
    @JsonProperty("intv2FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv2FormEsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv2ChronologiesShared;
    @JsonProperty("intv2QAShared")
    private List<UploadCaseDocumentCollection> intv2QaShared;
    private List<UploadCaseDocumentCollection> intv2StmtsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv2SummariesShared;
    @JsonProperty("intv2FormsHShared")
    private List<UploadCaseDocumentCollection> intv2FormHsShared;
    private List<UploadCaseDocumentCollection> intv2ExpertEvidenceShared;
    private List<UploadCaseDocumentCollection> intv2CorrespDocsShared;
    private List<UploadCaseDocumentCollection> intv2OtherShared;

    private List<UploadCaseDocumentCollection> intv3HearingBundlesShared;
    @JsonProperty("intv3FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv3FormEsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv3ChronologiesShared;
    @JsonProperty("intv3QAShared")
    private List<UploadCaseDocumentCollection> intv3QaShared;
    private List<UploadCaseDocumentCollection> intv3StmtsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv3SummariesShared;
    @JsonProperty("intv3FormsHShared")
    private List<UploadCaseDocumentCollection> intv3FormHsShared;
    private List<UploadCaseDocumentCollection> intv3ExpertEvidenceShared;
    private List<UploadCaseDocumentCollection> intv3CorrespDocsShared;
    private List<UploadCaseDocumentCollection> intv3OtherShared;

    private List<UploadCaseDocumentCollection> intv4HearingBundlesShared;
    @JsonProperty("intv4FormEExhibitsShared")
    private List<UploadCaseDocumentCollection> intv4FormEsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv4ChronologiesShared;
    @JsonProperty("intv4QAShared")
    private List<UploadCaseDocumentCollection> intv4QaShared;
    private List<UploadCaseDocumentCollection> intv4StmtsExhibitsShared;
    private List<UploadCaseDocumentCollection> intv4SummariesShared;
    @JsonProperty("intv4FormsHShared")
    private List<UploadCaseDocumentCollection> intv4FormHsShared;
    private List<UploadCaseDocumentCollection> intv4ExpertEvidenceShared;
    private List<UploadCaseDocumentCollection> intv4CorrespDocsShared;
    private List<UploadCaseDocumentCollection> intv4OtherShared;


    @JsonIgnore
    public List<UploadCaseDocumentCollection> getAllManageableCollections() {
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
                respCorrespondenceDocsCollShared, respOtherCollectionShared, intv1Summaries, intv1Chronologies,
                intv1CorrespDocs, intv1ExpertEvidence, intv1FormEsExhibits, intv1FormHs, intv1HearingBundles,
                intv1Other, intv1Qa, intv1StmtsExhibits, intv2Summaries, intv2Chronologies, intv2CorrespDocs,
                intv2ExpertEvidence, intv2FormEsExhibits, intv2FormHs, intv2HearingBundles, intv2Other, intv2Qa,
                intv2StmtsExhibits, intv3Summaries, intv3Chronologies, intv3CorrespDocs, intv3ExpertEvidence,
                intv3FormEsExhibits, intv3FormHs, intv3HearingBundles, intv3Other, intv3Qa, intv3StmtsExhibits,
                intv4Summaries, intv4Chronologies, intv4CorrespDocs, intv4ExpertEvidence, intv4FormEsExhibits,
                intv4FormHs, intv4HearingBundles, intv4Other, intv4Qa, intv4StmtsExhibits, intv1FdrCaseDocuments,
                intv2FdrCaseDocuments, intv3FdrCaseDocuments, intv4FdrCaseDocuments)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<UploadCaseDocumentCollection> getDocumentCollectionPerType(
        CaseDocumentCollectionType collectionType) {

        return switch (collectionType) {
            case APPLICANT_CORRESPONDENCE_COLLECTION -> appCorrespondenceCollection = getNonNull(appCorrespondenceCollection);
            case APPLICANT_CORRESPONDENCE_DOC_COLLECTION -> appCorrespondenceDocsCollection = getNonNull(appCorrespondenceDocsCollection);
            case APPLICANT_FR_FORM_COLLECTION -> appFrFormsCollection = getNonNull(appFrFormsCollection);
            case APPLICANT_EVIDENCE_COLLECTION -> appEvidenceCollection = getNonNull(appEvidenceCollection);
            case APPLICANT_TRIAL_BUNDLE_COLLECTION -> appTrialBundleCollection = getNonNull(appTrialBundleCollection);
            case APPLICANT_CONFIDENTIAL_DOCS_COLLECTION ->
                appConfidentialDocsCollection = getNonNull(appConfidentialDocsCollection);
            case RESPONDENT_CORRESPONDENCE_COLLECTION -> respCorrespondenceCollection = getNonNull(respCorrespondenceCollection);
            case RESPONDENT_FR_FORM_COLLECTION -> respFrFormsCollection = getNonNull(respFrFormsCollection);
            case RESPONDENT_EVIDENCE_COLLECTION -> respEvidenceCollection = getNonNull(respEvidenceCollection);
            case RESPONDENT_TRIAL_BUNDLE_COLLECTION ->
                respTrialBundleCollection = getNonNull(respTrialBundleCollection);
            case RESPONDENT_CONFIDENTIAL_DOCS_COLLECTION ->
                respConfidentialDocsCollection = getNonNull(respConfidentialDocsCollection);
            case APP_HEARING_BUNDLES_COLLECTION ->
                appHearingBundlesCollection = getNonNull(appHearingBundlesCollection);
            case APP_FORM_E_EXHIBITS_COLLECTION -> appFormEExhibitsCollection = getNonNull(appFormEExhibitsCollection);
            case APP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                appChronologiesCollection = getNonNull(appChronologiesCollection);
            case APP_QUESTIONNAIRES_ANSWERS_COLLECTION -> appQaCollection = getNonNull(appQaCollection);
            case APP_STATEMENTS_EXHIBITS_COLLECTION ->
                appStatementsExhibitsCollection = getNonNull(appStatementsExhibitsCollection);
            case APP_CASE_SUMMARIES_COLLECTION -> appCaseSummariesCollection = getNonNull(appCaseSummariesCollection);
            case APP_FORMS_H_COLLECTION -> appFormsHCollection = getNonNull(appFormsHCollection);
            case APP_EXPERT_EVIDENCE_COLLECTION ->
                appExpertEvidenceCollection = getNonNull(appExpertEvidenceCollection);
            case APP_OTHER_COLLECTION -> appOtherCollection = getNonNull(appOtherCollection);
            case RESP_HEARING_BUNDLES_COLLECTION ->
                respHearingBundlesCollection = getNonNull(respHearingBundlesCollection);
            case RESP_FORM_E_EXHIBITS_COLLECTION ->
                respFormEExhibitsCollection = getNonNull(respFormEExhibitsCollection);
            case RESP_CHRONOLOGIES_STATEMENTS_COLLECTION ->
                respChronologiesCollection = getNonNull(respChronologiesCollection);
            case RESP_QUESTIONNAIRES_ANSWERS_COLLECTION -> respQaCollection = getNonNull(respQaCollection);
            case RESP_STATEMENTS_EXHIBITS_COLLECTION ->
                respStatementsExhibitsCollection = getNonNull(respStatementsExhibitsCollection);
            case RESP_CASE_SUMMARIES_COLLECTION ->
                respCaseSummariesCollection = getNonNull(respCaseSummariesCollection);
            case RESP_FORM_H_COLLECTION -> respFormsHCollection = getNonNull(respFormsHCollection);
            case RESP_EXPERT_EVIDENCE_COLLECTION ->
                respExpertEvidenceCollection = getNonNull(respExpertEvidenceCollection);
            case RESP_CORRESPONDENCE_COLLECTION -> respCorrespondenceDocsColl = getNonNull(respCorrespondenceDocsColl);
            case RESP_OTHER_COLLECTION -> respOtherCollection = getNonNull(respOtherCollection);
            case CONTESTED_UPLOADED_DOCUMENTS -> uploadCaseDocument = getNonNull(uploadCaseDocument);
            case CONTESTED_FDR_CASE_DOCUMENT_COLLECTION ->
                fdrCaseDocumentCollection = getNonNull(fdrCaseDocumentCollection);
            case INTERVENER_ONE_SUMMARIES_COLLECTION -> intv1Summaries = getNonNull(intv1Summaries);
            case INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv1Chronologies = getNonNull(intv1Chronologies);
            case INTERVENER_ONE_CORRESPONDENCE_COLLECTION -> intv1CorrespDocs = getNonNull(intv1CorrespDocs);
            case INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION -> intv1ExpertEvidence = getNonNull(intv1ExpertEvidence);
            case INTERVENER_ONE_FORM_E_EXHIBITS_COLLECTION -> intv1FormEsExhibits = getNonNull(intv1FormEsExhibits);
            case INTERVENER_ONE_FORM_H_COLLECTION -> intv1FormHs = getNonNull(intv1FormHs);
            case INTERVENER_ONE_HEARING_BUNDLES_COLLECTION -> intv1HearingBundles = getNonNull(intv1HearingBundles);
            case INTERVENER_ONE_OTHER_COLLECTION -> intv1Other = getNonNull(intv1Other);
            case INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv1Qa = getNonNull(intv1Qa);
            case INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION -> intv1StmtsExhibits = getNonNull(intv1StmtsExhibits);
            case INTERVENER_TWO_SUMMARIES_COLLECTION -> intv2Summaries = getNonNull(intv2Summaries);
            case INTERVENER_TWO_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv2Chronologies = getNonNull(intv2Chronologies);
            case INTERVENER_TWO_CORRESPONDENCE_COLLECTION -> intv2CorrespDocs = getNonNull(intv2CorrespDocs);
            case INTERVENER_TWO_EXPERT_EVIDENCE_COLLECTION -> intv2ExpertEvidence = getNonNull(intv2ExpertEvidence);
            case INTERVENER_TWO_FORM_E_EXHIBITS_COLLECTION -> intv2FormEsExhibits = getNonNull(intv2FormEsExhibits);
            case INTERVENER_TWO_FORM_H_COLLECTION -> intv2FormHs = getNonNull(intv2FormHs);
            case INTERVENER_TWO_HEARING_BUNDLES_COLLECTION -> intv2HearingBundles = getNonNull(intv2HearingBundles);
            case INTERVENER_TWO_OTHER_COLLECTION -> intv2Other = getNonNull(intv2Other);
            case INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv2Qa = getNonNull(intv2Qa);
            case INTERVENER_TWO_STATEMENTS_EXHIBITS_COLLECTION -> intv2StmtsExhibits = getNonNull(intv2StmtsExhibits);
            case INTERVENER_THREE_SUMMARIES_COLLECTION -> intv3Summaries = getNonNull(intv3Summaries);
            case INTERVENER_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv3Chronologies = getNonNull(intv3Chronologies);
            case INTERVENER_THREE_CORRESPONDENCE_COLLECTION -> intv3CorrespDocs = getNonNull(intv3CorrespDocs);
            case INTERVENER_THREE_EXPERT_EVIDENCE_COLLECTION -> intv3ExpertEvidence = getNonNull(intv3ExpertEvidence);
            case INTERVENER_THREE_FORM_E_EXHIBITS_COLLECTION -> intv3FormEsExhibits = getNonNull(intv3FormEsExhibits);
            case INTERVENER_THREE_FORM_H_COLLECTION -> intv3FormHs = getNonNull(intv3FormHs);
            case INTERVENER_THREE_HEARING_BUNDLES_COLLECTION -> intv3HearingBundles = getNonNull(intv3HearingBundles);
            case INTERVENER_THREE_OTHER_COLLECTION -> intv3Other = getNonNull(intv3Other);
            case INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv3Qa = getNonNull(intv3Qa);
            case INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION -> intv3StmtsExhibits = getNonNull(intv3StmtsExhibits);
            case INTERVENER_FOUR_SUMMARIES_COLLECTION -> intv4Summaries = getNonNull(intv4Summaries);
            case INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION -> intv4Chronologies = getNonNull(intv4Chronologies);
            case INTERVENER_FOUR_CORRESPONDENCE_COLLECTION -> intv4CorrespDocs = getNonNull(intv4CorrespDocs);
            case INTERVENER_FOUR_EXPERT_EVIDENCE_COLLECTION -> intv4ExpertEvidence = getNonNull(intv4ExpertEvidence);
            case INTERVENER_FOUR_FORM_E_EXHIBITS_COLLECTION -> intv4FormEsExhibits = getNonNull(intv4FormEsExhibits);
            case INTERVENER_FOUR_FORM_H_COLLECTION -> intv4FormHs = getNonNull(intv4FormHs);
            case INTERVENER_FOUR_HEARING_BUNDLES_COLLECTION -> intv4HearingBundles = getNonNull(intv4HearingBundles);
            case INTERVENER_FOUR_OTHER_COLLECTION -> intv4Other = getNonNull(intv4Other);
            case INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION -> intv4Qa = getNonNull(intv4Qa);
            case INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION -> intv4StmtsExhibits = getNonNull(intv4StmtsExhibits);
            case INTERVENER_ONE_FDR_DOCS_COLLECTION -> intv1FdrCaseDocuments = getNonNull(intv1FdrCaseDocuments);
            case INTERVENER_TWO_FDR_DOCS_COLLECTION -> intv2FdrCaseDocuments = getNonNull(intv2FdrCaseDocuments);
            case INTERVENER_THREE_FDR_DOCS_COLLECTION -> intv3FdrCaseDocuments = getNonNull(intv3FdrCaseDocuments);
            case INTERVENER_FOUR_FDR_DOCS_COLLECTION -> intv4FdrCaseDocuments = getNonNull(intv4FdrCaseDocuments);
            case CONFIDENTIAL_DOCS_COLLECTION -> confidentialDocumentCollection =
                getNonNull(confidentialDocumentCollection);
        };
    }

    private List<UploadCaseDocumentCollection> getNonNull(List<UploadCaseDocumentCollection> collection) {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        return collection;
    }
}

