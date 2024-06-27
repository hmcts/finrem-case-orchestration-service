package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRefusalHolder implements CaseDocumentsDiscovery {
    private String orderRefusalAfterText;
    private List<OrderRefusalOption> orderRefusal;
    private String orderRefusalOther;
    private CaseDocument orderRefusalDocs;
    @JsonProperty("orderRefusalJudge")
    private JudgeType orderRefusalJudge;
    private String orderRefusalJudgeName;
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderRefusalDate;
    private String orderRefusalAddComments;

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(orderRefusalDocs)
            .map(List::of)
            .orElse(List.of());
    }
}
