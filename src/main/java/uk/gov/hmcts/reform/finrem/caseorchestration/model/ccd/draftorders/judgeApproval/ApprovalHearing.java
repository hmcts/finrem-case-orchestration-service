package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeApproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalHearing {
    @JsonProperty("hearingType")
    private String hearingType;

//    @JsonProperty("draftOrdersForApprovalCollection")
//    private List<DraftOrdersForApprovalCollection> draftOrdersForApprovalCollection;
}
