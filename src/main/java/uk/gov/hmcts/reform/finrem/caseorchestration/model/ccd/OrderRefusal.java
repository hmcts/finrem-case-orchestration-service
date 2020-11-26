package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderRefusal {
    @JsonProperty("orderRefusalAfterText")
    private String orderRefusalAfterText;
    @JsonProperty("orderRefusal")
    private List<String> orderRefusal;
    @JsonProperty("orderRefusalOther")
    private String orderRefusalOther;
    @JsonProperty("orderRefusalDocs")
    private CaseDocument orderRefusalDocs;
    @JsonProperty("orderRefusalJudge")
    private String orderRefusalJudge;
    @JsonProperty("orderRefusalJudgeName")
    private String orderRefusalJudgeName;
    @JsonProperty("orderRefusalDate")
    private LocalDate orderRefusalDate;
    @JsonProperty("orderRefusalAddComments")
    private String orderRefusalAddComments;
}
