package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDetails {
    @JsonProperty("id")
    private String caseId;
    @JsonProperty("jurisdiction")
    private String jurisdiction;
    @JsonProperty("state")
    private String state;
    @JsonProperty("case_data")
    private CaseData caseData;
}
