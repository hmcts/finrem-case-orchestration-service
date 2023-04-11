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
}
