package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadSuggestedDraftOrder {
    @JsonProperty("confirmUploadedDocuments")
    private DynamicMultiSelectList confirmUploadedDocuments;

    @JsonProperty("uploadParty")
    private String uploadParty;

    @JsonProperty("uploadOrdersOrPSAs")
    private List<String> uploadOrdersOrPSAs;

    @JsonProperty("suggestedDraftOrderCollection")
    private List<SuggestedDraftOrderCollection> suggestedDraftOrderCollection;

    @JsonProperty("suggestedPSACollection")
    private List<SuggestedPensionSharingAnnexCollection> suggestedPSACollection;
}
