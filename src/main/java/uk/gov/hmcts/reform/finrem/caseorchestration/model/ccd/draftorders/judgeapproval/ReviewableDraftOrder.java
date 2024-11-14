package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewableDraftOrder {

    private String hearingInfo;

    private YesOrNo hasAttachment;

    @JsonProperty("document")
    private CaseDocument document;

    @JsonProperty("judgeDecision")
    private JudgeDecision judgeDecision;

    @JsonProperty("attachments")
    private List<CaseDocumentCollection> attachments;

    @JsonProperty("isFinalOrder")
    private YesOrNo isFinalOrder;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedDate;

    public YesOrNo getHasAttachment() {
        return YesOrNo.forValue(!ofNullable(attachments).orElse(List.of()).isEmpty());
    }
}
