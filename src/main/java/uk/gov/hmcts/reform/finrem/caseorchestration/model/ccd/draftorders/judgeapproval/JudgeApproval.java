package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;

import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudgeApproval {

    private JudgeApprovalDocType docType;

    private String title;

    private String inlineDocType;

    private String hearingInfo;

    private YesOrNo hasAttachment;

    @JsonProperty("document")
    private CaseDocument document;

    @JsonProperty("amendedDocument")
    private CaseDocument amendedDocument;

    @JsonProperty("judgeDecision")
    private JudgeDecision judgeDecision;

    @JsonProperty("attachments")
    private List<CaseDocumentCollection> attachments;

    @JsonProperty("isFinalOrder")
    private DynamicMultiSelectList isFinalOrder;

    @JsonProperty("changesRequestedByJudge")
    private String changesRequestedByJudge;

    @JsonIgnore
    private SortKey sortKey;

    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(!ofNullable(attachments).orElse(List.of()).isEmpty());
    }

}
