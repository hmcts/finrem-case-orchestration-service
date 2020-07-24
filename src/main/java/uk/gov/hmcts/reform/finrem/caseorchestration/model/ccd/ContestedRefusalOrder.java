package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContestedRefusalOrder {

    /*
    @JsonProperty("reasonForRefusal")
    private List<String> reasonForRefusal;

    @JsonProperty("judgeType")
    private String judgeType;

    @JsonProperty("judgeName")
    private String judgeName;

    @JsonProperty("dateOfOrder")
    private Date dateOfOrder;
     */

    @JsonProperty("notApprovedDocument")
    private CaseDocument notApprovedDocument;
}
