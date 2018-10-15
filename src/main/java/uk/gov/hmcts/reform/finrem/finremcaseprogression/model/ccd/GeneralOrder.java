package uk.gov.hmcts.reform.finrem.finremcaseprogression.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GeneralOrder {
    @JsonProperty("generalOrder_order")
    private String generalOrder;
    @JsonProperty("generalOrder_documentUpload")
    private CaseDocument generalOrderDocumentUpload;
    @JsonProperty("generalOrder_judgeList")
    private String generalOrderJudgeType;
    @JsonProperty("generalOrder_judgeName")
    private String generalOrderJudgeName;
    @JsonProperty("generalOrder_dateOfOrder")
    private LocalDate generalOrderDate;
    @JsonProperty("generalOrder_comments")
    private String generalOrderComments;
}
