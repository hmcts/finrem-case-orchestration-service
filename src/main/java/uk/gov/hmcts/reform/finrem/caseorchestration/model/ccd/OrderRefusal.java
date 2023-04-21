package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderRefusalDate;
    @JsonProperty("orderRefusalAddComments")
    private String orderRefusalAddComments;
}
