package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewablePsa {

    private String hearingInfo;

    @JsonProperty("document")
    private CaseDocument document;

    @JsonProperty("judgeDecision")
    private JudgeDecision judgeDecision;
}
