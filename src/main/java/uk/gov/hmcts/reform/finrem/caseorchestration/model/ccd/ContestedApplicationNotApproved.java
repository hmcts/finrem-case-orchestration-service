package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContestedApplicationNotApproved {

    @JsonProperty("reasonForRefusal")
    private List<String> reasonForRefusal;

    @JsonProperty("othersTextOrders")
    private String othersTextOrders;

    @JsonProperty("judgeType")
    private String judgeType;

    @JsonProperty("judgeName")
    private String judgeName;

    @JsonProperty("dateOfOrder")
    private Date dateOfOrder;

    @JsonProperty("notApprovedDocument")
    private CaseDocument notApprovedDocument;
}
