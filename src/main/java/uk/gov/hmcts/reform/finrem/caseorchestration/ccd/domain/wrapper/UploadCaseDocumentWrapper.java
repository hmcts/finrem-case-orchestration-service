package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain.UploadCaseDocumentCollection;

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
}
