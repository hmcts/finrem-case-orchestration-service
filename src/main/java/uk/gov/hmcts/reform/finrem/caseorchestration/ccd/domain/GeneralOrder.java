package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralOrder {
    @JsonProperty("generalOrder_addressTo")
    private String generalOrderAddressTo;
    @JsonProperty("generalOrder_order")
    private String generalOrderOrder;
    @JsonProperty("generalOrder_documentUpload")
    private Document generalOrderDocumentUpload;
    @JsonProperty("generalOrder_judgeList")
    private JudgeType generalOrderJudgeType;
    @JsonProperty("generalOrder_judgeName")
    private String generalOrderJudgeName;
    @JsonProperty("generalOrder_dateOfOrder")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate generalOrderDateOfOrder;
    @JsonProperty("generalOrder_comments")
    private String generalOrderComments;
    private String generalOrderText;
}
