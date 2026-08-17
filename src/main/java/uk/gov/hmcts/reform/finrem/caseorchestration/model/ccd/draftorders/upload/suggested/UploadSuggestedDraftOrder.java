package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FRUploadOrdersOrPsas;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_uploadSuggestedDraftOrder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadSuggestedDraftOrder {
    @CCD(
            label = "Confirm the uploaded documents are for the case",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList
    )
    @JsonProperty("confirmUploadedDocuments")
    private DynamicMultiSelectList confirmUploadedDocuments;

    @CCD(
            label = "Who are you uploading this on behalf of?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList
    )
    @JsonProperty("uploadParty")
    private DynamicRadioList uploadParty;

    @CCD(
            label = "What are you uploading?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FR_UploadOrdersOrPsas",
            typeParameterClass = FRUploadOrdersOrPsas.class
    )
    @JsonProperty("uploadOrdersOrPsas")
    private List<String> uploadOrdersOrPsas;

    @CCD(
            label = "Upload draft order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_SuggestedDraftOrders"
    )
    @JsonProperty("suggestedDraftOrderCollection")
    private List<UploadSuggestedDraftOrderCollection> uploadSuggestedDraftOrderCollection;

    @CCD(
            label = "Pension Sharing Annexes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_SuggestedPensionSharingAnnexes"
    )
    @JsonProperty("suggestedPsaCollection")
    private List<SuggestedPensionSharingAnnexCollection> suggestedPsaCollection;

    @JsonIgnore
    private OrderFiledBy orderFiledBy;
}
