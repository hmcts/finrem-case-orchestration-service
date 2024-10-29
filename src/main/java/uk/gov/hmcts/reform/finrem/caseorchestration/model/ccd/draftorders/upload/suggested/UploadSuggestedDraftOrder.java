package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;

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
    private DynamicRadioList uploadParty;

    @JsonProperty("uploadOrdersOrPsas")
    private List<String> uploadOrdersOrPsas;

    @JsonProperty("suggestedDraftOrderCollection")
    private List<UploadSuggestedDraftOrderCollection> uploadSuggestedDraftOrderCollection;

    @JsonProperty("suggestedPsaCollection")
    private List<SuggestedPensionSharingAnnexCollection> suggestedPsaCollection;
}
