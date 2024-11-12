package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeApproval {

    private YesOrNo showMoreDraftOrdersMessage;
    private ReviewableDraftOrder reviewableDraftOrder1;
    private ReviewableDraftOrder reviewableDraftOrder2;
    private ReviewableDraftOrder reviewableDraftOrder3;
    private ReviewableDraftOrder reviewableDraftOrder4;
    private ReviewableDraftOrder reviewableDraftOrder5;

    private YesOrNo showMorePsasMessage;
    private ReviewablePsa reviewablePsa1;
    private ReviewablePsa reviewablePsa2;
    private ReviewablePsa reviewablePsa3;
    private ReviewablePsa reviewablePsa4;
    private ReviewablePsa reviewablePsa5;

}
