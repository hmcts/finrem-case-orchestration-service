package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadAgreedDraftOrder {
    @JsonProperty("confirmUploadedDocuments")
    private DynamicMultiSelectList confirmUploadedDocuments;

    @JsonProperty("hearingDetails")
    private DynamicList hearingDetails;

    @JsonProperty("judgeKnownAtHearing")
    private YesOrNo judgeKnownAtHearing;

    @JsonProperty("modifyPrevious")
    private String judge;

    @JsonProperty("randomJavaField")
    private DynamicRadioList randomJavaField;

    @JsonProperty("uploadParty")
    private DynamicRadioList uploadParty;

    @JsonProperty("uploadOrdersOrPsas")
    private List<String> uploadOrdersOrPsas;

    @JsonProperty("agreedDraftOrderCollection")
    private List<UploadAgreedDraftOrderCollection> uploadAgreedDraftOrderCollection;

    @JsonProperty("agreedPsaCollection")
    private List<AgreedPensionSharingAnnexCollection> agreedPsaCollection;

    @JsonIgnore
    private OrderFiledBy orderFiledBy;
}
