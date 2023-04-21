package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.util.List;

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
    private List<UploadCaseDocumentCollection> manageCaseDocumentCollection;

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
}
